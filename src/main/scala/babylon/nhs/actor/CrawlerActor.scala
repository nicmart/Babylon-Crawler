package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Status}
import akka.stream.ActorMaterializer

import scala.concurrent.duration._
import babylon.nhs.actor.ProxyActor.Message
import babylon.nhs.actor.SupervisorActor.CrawlingDone
import babylon.nhs.scraper._
import CrawlerActor._

/**
  * The main actor responsible for crawling.
  *
  * @param requestsPerSecond The maximum number of active scrapers per second
  * @param timeout After this amount of time of inactivity the system will shutdown.
  */
class CrawlerActor(
    requestsPerSecond: Int = 10,
    timeout: FiniteDuration = 10.seconds
) extends Actor with ActorLogging {

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

    def receive: Receive = active(CrawlerState())

    def active(crawlerState: CrawlerState): Receive = {

        case StartCrawling(uri, state) => {
            log.info("Starting scraping {}", uri.toString)
            log.info("{} active links", crawlerState.activeScrapers.size)
            changeState(scrapeLinks(crawlerState, state, List(uri)))
        }

        case Scraped(result, state) => {
            log.info("Finished scraping {}", result.uri.toString)
            log.info("{} active scrapers", crawlerState.activeScrapers.size)
            log.info("Links found: {}", result.links.toString())
            context.parent ! SupervisorActor.Scraped(result, state)
            changeState(scrapeLinks(crawlerState.visit(result.uri), state.next(result), result.links))
        }

        case Status.Failure(cause) => {
            log.info("There has been a failure with message: {}", cause.toString)
        }

        case ReceiveTimeout =>
            log.info("Timeout Received")
            for (scraper <- crawlerState.activeScrapers.values) {
                context.stop(scraper)
            }
            changeState(crawlerState.copy(activeScrapers = Map.empty))
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
            case (accState, link) => if (accState.isNew(link)) {
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
        context become active(crawlerState)
        if (crawlerState.isFinal) {
            context.parent ! CrawlingDone
        }
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

/**
  * This is the immutable state of the crawler.
  * It consists of a map of urls -> scrapers, and a set of visited links
  */
case class CrawlerState(
    activeScrapers: Map[String, ActorRef] = Map.empty,
    visitedLinks: Set[String] = Set.empty
) {
    /**
      * Add a new active scraper
      */
    def addScraper(scraper: ActorRef, uri: URI): CrawlerState =
        copy(activeScrapers = activeScrapers + (normaliseUri(uri) -> scraper))

    /**
      * Mark a uri as visited
      */
    def visit(uri: URI): CrawlerState = {
        val normalisedUri = normaliseUri(uri)
        copy(visitedLinks = visitedLinks + normalisedUri, activeScrapers = activeScrapers - normalisedUri)
    }

    /**
      * Check if we already got that uri
      */
    def isNew(uri: URI): Boolean = {
        val normalisedUri = normaliseUri(uri)
        !visitedLinks.contains(normalisedUri) && !activeScrapers.contains(normalisedUri)
    }

    /**
      * When there are no active scrapers, it means we are done
      */
    def isFinal: Boolean = activeScrapers.isEmpty

    private def normaliseUri(uri: URI): String = uri.toString.toLowerCase
}