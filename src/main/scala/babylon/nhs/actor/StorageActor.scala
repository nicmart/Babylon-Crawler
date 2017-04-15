package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.Controller.StorageReady
import babylon.nhs.actor.StorageActor.{Done, Store}
import babylon.nhs.scraper.ScraperResult

/**
  * Created by nic on 15/04/2017.
  */
class StorageActor extends Actor with ActorLogging {
    def receive: Receive = active(Map.empty)
    def active(pages: Map[URI, ScraperResult]): Receive = {
        case Store(result) =>
            log.info("Storing result for {}", result.uri.toString)
            val updatePages = pages + (result.uri -> result)
            context become active(updatePages)
        case Done =>
            sender ! StorageReady(pages)
            context.stop(self)
    }
}

object StorageActor {
    sealed trait Message
    case class Store(result: ScraperResult) extends Message
    case object Done extends Message
}


