package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import babylon.nhs.actor.CrawlerActor.StartCrawling
import babylon.nhs.actor.DumperActor.Dump
import babylon.nhs.actor.OutputActor.{AddOutput, GetOutput}
import babylon.nhs.scraper.{ScraperResult, ScraperState}
import babylon.nhs.writer._
import babylon.nhs.output.{ResultToOutput}
import babylon.nhs.output.Output.PageList

/**
  * Entry point of our actor system.
  * This actor is responsible of supervising the crawler, the output and dumper actors.
  *
  * @param writer The writer passed to the dumper actor
  * @param resultToOutput The ScrapeResult to Output conversion used by the output actor
  */
class SupervisorActor(writer: Writer[PageList], resultToOutput: ResultToOutput) extends Actor with ActorLogging {
    import SupervisorActor._

    val crawler: ActorRef = context.actorOf(Props(new CrawlerActor), "crawler")
    val output: ActorRef = context.actorOf(Props(new OutputActor(resultToOutput)), "output")
    val dumper: ActorRef = context.actorOf(Props(new DumperActor(writer)), "dumper")

    def receive: Receive = {
        case Start(uri, state) => crawler ! StartCrawling(uri, state)
        case Scraped(result, state) =>
            if (state.isLeaf) output ! AddOutput(result)
        case CrawlingDone =>
            crawler ! PoisonPill
            output ! GetOutput
        case OutputReady(storage) =>
            output ! PoisonPill
            dumper ! Dump(storage)
        case DumpReady =>
            context.stop(self)
            context.system.terminate()
    }
}

object SupervisorActor {
    sealed trait Message

    /**
      * Message received to start the system
      */
    case class Start(uri: URI, state: ScraperState) extends Message

    /**
      * Message received after a page has been scraped
      */
    case class Scraped(result: ScraperResult, state: ScraperState) extends Message

    /**
      * Message received when the output is ready to be dumped
      */
    case class OutputReady(storage: PageList) extends Message

    /**
      * Message received when the dump actor has done his job
      */
    case object DumpReady extends Message

    /**
      * Message received when the crawler has crawled all the pages
      */
    case object CrawlingDone extends Message
}
