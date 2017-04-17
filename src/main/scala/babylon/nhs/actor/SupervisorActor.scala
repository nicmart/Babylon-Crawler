package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import babylon.nhs.actor.CrawlerActor.StartCrawling
import babylon.nhs.actor.DumperActor.Dump
import babylon.nhs.actor.OutputActor.{AddOutput, GetOutput}
import babylon.nhs.scraper.{ScraperResult, ScraperState}
import babylon.nhs.serialiser.JsonStorageSerialiser
import babylon.nhs.writer.SerialiserWriter
import babylon.nhs.writer._
import babylon.nhs.output.{Output, ResultToOutput}
import babylon.nhs.output.Output.PageList

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
class SupervisorActor(writer: Writer[PageList], resultToOutput: ResultToOutput) extends Actor with ActorLogging {
    import SupervisorActor._

    val crawler: ActorRef = context.actorOf(Props(new CrawlerActor), "crawler")
    val output: ActorRef = context.actorOf(Props(new OutputActor(resultToOutput)), "output")
    val dumper: ActorRef = context.actorOf(Props(new DumperActor(writer)), "dumper")

    def receive: Receive = active(Set.empty)

    def active(linksScraped: Set[URI]): Receive = {
        case Start(uri, state) => crawler ! StartCrawling(uri, state)
        case Scraped(result, state) =>
            if (state.isLeaf) output ! AddOutput(result)
            context become active(linksScraped + result.uri.normalize())
        case CrawlingDone =>
            crawler ! PoisonPill
            output ! GetOutput
        case OutputReady(storage) =>
            output ! PoisonPill
            dumper ! Dump(storage)
        case DumpReady =>
            context.system.terminate()
    }
}

object SupervisorActor {
    sealed trait Message
    case class Start(uri: URI, state: ScraperState) extends Message
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message
    case class OutputReady(storage: PageList) extends Message
    case object DumpReady extends Message
    case object CrawlingDone extends Message
}
