package babylon.nhs.actor

import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.StorageActor.{GetStorage, Store}
import babylon.nhs.actor.Supervisor.StorageReady
import babylon.nhs.scraper.ScraperResult
import babylon.nhs.output.Output.{PageElement, PageList}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
/**
  * Created by nic on 15/04/2017.
  */
class StorageActor extends Actor with ActorLogging {
    def receive: Receive = active(List.empty)
    def active(pages: PageList): Receive = {
        case Store(result) =>
            log.info("Storing result for {}", result.uri.toString)
            context become active(pageElement(result) :: pages)
        case GetStorage =>
            sender ! StorageReady(pages)
    }

    private def pageElement(result: ScraperResult) = PageElement(
        result.uri.toString,
        result.document.title,
        result.document >> allText(".main-content")
    )
}

object StorageActor {
    sealed trait Message
    case class Store(result: ScraperResult) extends Message
    case object GetStorage extends Message
}


