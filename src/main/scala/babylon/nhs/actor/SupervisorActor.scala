package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import babylon.nhs.actor.CrawlerActor.StartCrawling
import babylon.nhs.actor.DumperActor.Dump
import babylon.nhs.actor.StorageActor.{GetStorage, Store}
import babylon.nhs.scraper.{ScraperResult, ScraperState}
import babylon.nhs.serialiser.JsonStorageSerialiser
import babylon.nhs.writer.SerialiserWriter
import babylon.nhs.writer._
import babylon.nhs.output.Output
import babylon.nhs.output.Output.PageList

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
class SupervisorActor(writer: Writer[PageList]) extends Actor with ActorLogging {
    import SupervisorActor._

    val crawler: ActorRef = context.actorOf(Props(new CrawlerActor), "crawler")
    val store: ActorRef = context.actorOf(Props(new StorageActor), "storage")
    val dumper: ActorRef = context.actorOf(
        Props(new DumperActor(writer)),
        "dumper"
    )

    def receive: Receive = active(Set.empty)

    def active(linksScraped: Set[URI]): Receive = {
        case Start(uri, state) => crawler ! StartCrawling(uri, state)
        case Scraped(result, state) =>
            if (state.isLeaf) store ! Store(result)
            context become active(linksScraped + result.uri.normalize())
        case DoneCrawling =>
            crawler ! PoisonPill
            store ! GetStorage
        case StorageReady(storage) =>
            store ! PoisonPill
            dumper ! Dump(storage)
        case DumpReady =>
            context.system.terminate()
    }
}

object SupervisorActor {
    sealed trait Message
    case class Start(uri: URI, state: ScraperState) extends Message
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message
    case class StorageReady(storage: PageList) extends Message
    case object DumpReady extends Message
    case object DoneCrawling extends Message
}
