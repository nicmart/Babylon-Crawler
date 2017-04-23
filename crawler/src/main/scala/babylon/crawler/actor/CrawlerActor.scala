package babylon.crawler.actor

import java.net.URI
import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Status}
import akka.stream.ActorMaterializer
import babylon.crawler.actor.SupervisorActor.CrawlingDone
import babylon.crawler.scraper._
import babylon.crawler.actor.state.CrawlerState
import babylon.crawler.scraper.Scraper.ScraperFailure

/**
  * The main actor responsible for crawling.
  *
  * @param requestsPerSecond The maximum number of active scrapers per second
  * @param timeout After this amount of time of inactivity the system will shutdown.
  * @param maxAttempts The maximum number of scraping attempts per url
  */
class CrawlerActor(
    requestsPerSecond: Int = 10,
    timeout: FiniteDuration = 10.seconds,
    maxAttempts: Int = 3
) extends Actor with ActorLogging {

    import CrawlerActor._
    implicit val materializer = ActorMaterializer.create(context.system)

    /**
      * We send messages to scrapers through a proxy throttled actor.
      * In this way we have control on the number of calls per second to the target website
      */
    val scrapersProxy = ProxyActor.throttledProxy(
        context.actorOf(Props(new ProxyActor)),
        requestsPerSecond
    )

    /**
      * Set the timeout
      */
    override def preStart(): Unit = {
        context.setReceiveTimeout(timeout)
    }

    /**
      * We are initially in an empty crawler state
      */
    def receive: Receive = active(CrawlerState())

    /**
      * Returns a receive function in a new crawler state
      */
    def active(crawlerState: CrawlerState): Receive = {

        /**
          * Initial message to start the crawling
          */
        case StartCrawling(uri, state) => {
            changeState(scrapeLinks(crawlerState, state, List(uri)))
        }

        /**
          * A Scraper ended successfully his job
          */
        case Scraped(result, state) => {
            context.parent ! SupervisorActor.Scraped(result, state)
            changeState(scrapeLinks(crawlerState.visit(result.uri), state.next(result), result.links))
        }

        /**
          * A Scraper failed scraping a page
          */
        case Status.Failure(failure@ScraperFailure(uri, state, originalException)) => {
            logFailure(failure)
            if (crawlerState.attempts(uri) >= maxAttempts) {
                changeState(crawlerState.visit(uri).addError(failure))
            } else {
                log.info(s"Retrying scraping of '${uri.toString}'")
                changeState(scrapeLinks(crawlerState.failed(uri), state, List(uri)))
            }
        }

        /**
          * A timeout happened
          */
        case ReceiveTimeout =>
            log.info("Timeout Received")
            for (scraper <- crawlerState.activeScrapers.values) {
                context.stop(scraper)
            }
            changeState(crawlerState.copy(activeScrapers = Map.empty))

        case boh => log.info(boh.toString)
    }

    /**
      * Create new scrapers for each link, if the link is new,
      * and evolve the CrawlerState accordingly
      */
    private def scrapeLinks(
        crawlerState: CrawlerState,
        scraperState: ScraperState,
        links: List[URI]
    ): CrawlerState = {
        links.foldLeft(crawlerState) {
            case (accState, link) =>
                if (accState.isNew(link)) {
                    val scraper = context.actorOf(Props(new ScraperActor))
                    scrapersProxy ! ProxyActor.Message(scraper, ScraperActor.Scrape(link, scraperState), self)
                    accState.addScraper(scraper, link)
                } else accState
        }
    }

    /**
      * Change the state of the actor.
      * If the new state is final, notify the parent.
      */
    private def changeState(crawlerState: CrawlerState): Unit = {
        log.info("Visited {} pages", crawlerState.visitedLinks.size)
        context become active(crawlerState)
        if (crawlerState.isFinal) {
            context.parent ! CrawlingDone(crawlerState)
        }
    }

    private def logFailure(failure: ScraperFailure): Unit = {
        val url = failure.uri.toString
        val message = failure.originalException.getMessage
        log.info(s"There has been a failure scraping the page at '$url' with message '$message'")
    }
}

object CrawlerActor {

    sealed trait Message

    /**
      * Initial message. When received we start the crawling
      */
    case class StartCrawling(uri: URI, state: ScraperState) extends Message

    /**
      * Message received when a scraper is done.
      */
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message
}

