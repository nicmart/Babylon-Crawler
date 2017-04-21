package babylon.crawler.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import babylon.crawler.actor.CrawlerActor.StartCrawling
import babylon.crawler.actor.DumperActor.Dump
import babylon.crawler.actor.OutputActor.{AddOutput, GetOutput}
import babylon.crawler.scraper.{ScraperResult, ScraperState}
import babylon.crawler.writer._
import babylon.crawler.output.{ResultToOutput}
import babylon.crawler.output.Output.PageList

/**
  * Entry point of our actor system.
  * This actor is responsible of supervising the crawler, the output and dumper actors.
  *
  * @param writer The writer passed to the dumper actor
  * @param resultToOutput The ScrapeResult to Output conversion used by the output actor
  */
class SupervisorActor(
    writer: Writer[PageList],
    resultToOutput: ResultToOutput,
    pagesPerSecond: Int
) extends Actor with ActorLogging {
    import SupervisorActor._

    val crawler: ActorRef = context.actorOf(Props(new CrawlerActor(pagesPerSecond)), "crawler")
    val output: ActorRef = context.actorOf(Props(new OutputActor(resultToOutput)), "output")
    val dumper: ActorRef = context.actorOf(Props(new DumperActor(writer)), "dumper")

    def receive: Receive = {
        case Start(uri, state) =>
            crawler ! StartCrawling(uri, state)
        case Scraped(result, state) =>
            log.info("Scraped {}", result.uri.toASCIIString)
            output ! AddOutput(result)
        case CrawlingDone =>
            crawler ! PoisonPill
            output ! GetOutput
        case OutputReady(pageList) =>
            log.info("Scraped a total of {} pages", pageList.size)
            output ! PoisonPill
            dumper ! Dump(pageList)
        case DumpReady =>
            log.info("Dump ready. Goodbye!")
            dumper ! PoisonPill
            context.system.terminate()
            context.stop(self)
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
