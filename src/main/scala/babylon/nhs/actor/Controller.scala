package babylon.nhs.actor

import java.net.URI
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.actor.Status
import babylon.nhs.actor.Controller.{Scrape, Scraped, StorageReady}
import babylon.nhs.actor.StorageActor.{Done, Store}
import babylon.nhs.scraper._

/**
  * Created by nic on 15/04/2017.
  */
class Controller extends Actor with ActorLogging {

    def receive: Receive = active(
        Set.empty[ActorRef],
        context.actorOf(Props(new StorageActor))
    )

    def active(scraperActors: Set[ActorRef], storageActor: ActorRef): Receive = {

        case Scrape(uri, state) => {
            log.info("Starting scraping {}", uri.toString)
            val scraperActor = context.actorOf(Props(new ScraperActor))
            scraperActor ! ScraperActor.Scrape(uri, state)
            withScrapers(scraperActors + scraperActor, storageActor)
        }
        case Scraped(result, state) => {
            log.info("Finished scraping {}", result.uri.toString)
            log.info("Links found: {}", result.links.toString())
            if (state.isLeaf) storageActor ! Store(result)
            result.links.foreach { link =>
                self ! Scrape(link, state.next)
            }
            withScrapers(scraperActors - sender, storageActor)
        }

        case StorageReady(storage) => {
            log.info(storage.toString())
        }

        case Status.Failure(cause) => {
            log.info("There has been a failure with message: {}", cause.toString)
            withScrapers(scraperActors - sender, storageActor)
        }
    }

    private def withScrapers(scrapers: Set[ActorRef], storageActor: ActorRef): Unit = {
        context become active(scrapers, storageActor)
        if (scrapers.isEmpty) {
            //storageActor ! Done
        }
    }
}

object Controller {
    sealed trait Message
    case class Scrape(uri: URI, state: ScraperState) extends Message
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message
    case class StorageReady(storage: Map[URI, ScraperResult])
}
