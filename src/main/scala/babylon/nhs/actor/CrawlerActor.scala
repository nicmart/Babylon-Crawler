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
  * Created by nic on 15/04/2017.
  */
class CrawlerActor extends Actor with ActorLogging {

    implicit val materializer = ActorMaterializer.create(context.system)

    val scrapersProxy = ProxyActor.throttledProxy(
        context.actorOf(Props(new ProxyActor)),
        10
    )
    context.setReceiveTimeout(5.seconds)

    def receive: Receive = active(CrawlerState())

    def active(crawlerState: CrawlerState): Receive = {

        case StartCrawling(uri, state) => {
            log.info("Starting scraping {}", uri.toString)
            log.info("{} active scrapers", crawlerState.activeScrapers.size)
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
            log.info("received timeout")
            for (scraper <- crawlerState.activeScrapers.values) {
                context.stop(scraper)
            }
            changeState(crawlerState.copy(activeScrapers = Map.empty))
    }

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

    private def changeState(crawlerState: CrawlerState): Unit = {
        context become active(crawlerState)
        if (crawlerState.activeScrapers.isEmpty) {
            context.parent ! CrawlingDone
        }
    }
}

object CrawlerActor {
    sealed trait Message
    case class StartCrawling(uri: URI, state: ScraperState) extends Message
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message
}

case class CrawlerState(
    activeScrapers: Map[String, ActorRef] = Map.empty,
    visitedLinks: Set[String] = Set.empty
) {
    def addScraper(scraper: ActorRef, uri: URI): CrawlerState =
        copy(activeScrapers = activeScrapers + (normaliseUri(uri) -> scraper))

    def visit(uri: URI): CrawlerState = {
        val normalisedUri = normaliseUri(uri)
        copy(visitedLinks = visitedLinks + normalisedUri, activeScrapers = activeScrapers - normalisedUri)
    }

    def isNew(uri: URI): Boolean = {
        val normalisedUri = normaliseUri(uri)
        !visitedLinks.contains(normalisedUri) && !activeScrapers.contains(normalisedUri)
    }

    private def normaliseUri(uri: URI): String = uri.toString.toLowerCase
}