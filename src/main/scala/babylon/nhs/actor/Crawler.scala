package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.actor.Status
import babylon.nhs.actor.Supervisor.DoneCrawling
import babylon.nhs.scraper._

/**
  * Created by nic on 15/04/2017.
  */
class Crawler extends Actor with ActorLogging {

    import Crawler._

    def receive: Receive = active(CrawlerState())

    def active(crawlerState: CrawlerState): Receive = {

        case StartCrawling(uri, state) => {
            log.info("Starting scraping {}", uri.toString)
            val newScrapers = scrapeLinks(crawlerState, state, List(uri))
            to(crawlerState.withScrapers(newScrapers.toSet))
        }
        case Scraped(result, state) => {
            log.info("Finished scraping {}", result.uri.toString)
            log.info("Links found: {}", result.links.toString())
            context.parent ! Supervisor.Scraped(result, state)
            val newScrapers = scrapeLinks(crawlerState, state.next, result.links)

            to(
                crawlerState
                    .withScrapers(crawlerState.activeScrapers ++ newScrapers - sender)
                    .withLink(result.uri)
            )
        }

        case Status.Failure(cause) => {
            log.info("There has been a failure with message: {}", cause.toString)
            to(crawlerState.withScrapers(crawlerState.activeScrapers - sender))
        }
    }

    private def scrapeLinks(crawlerState: CrawlerState, state: ScraperState, links: List[URI]): List[ActorRef] = {
        for (link <- links if !crawlerState.isVisited(link)) yield {
            val scraper = context.actorOf(Props(new ScraperActor), s"scraper-${link.hashCode().toString}")
            scraper ! ScraperActor.Scrape(link, state)
            scraper
        }
    }

    private def to(crawlerState: CrawlerState): Unit = {
        context become active(crawlerState)
        if (crawlerState.activeScrapers.isEmpty) {
            context.parent ! DoneCrawling
        }
    }
}

object Crawler {
    sealed trait Message
    case class StartCrawling(uri: URI, state: ScraperState) extends Message
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message

    case class CrawlerState(
        activeScrapers: Set[ActorRef] = Set.empty,
        visitedLinks: Set[URI] = Set.empty
    ) {
        def withScrapers(scrapers: Set[ActorRef]): CrawlerState =
            copy(activeScrapers = scrapers)

        def withLink(uri: URI): CrawlerState =
            copy(visitedLinks = visitedLinks + uri.normalize())

        def isVisited(uri: URI): Boolean = visitedLinks.contains(uri.normalize())
    }
}
