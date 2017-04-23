package babylon.crawler.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import babylon.crawler.actor.CrawlerActor.StartCrawling
import babylon.crawler.actor.DumperActor.Dump
import babylon.crawler.actor.OutputActor.{AddOutput, GetOutput}
import babylon.crawler.actor.state.CrawlerState
import babylon.crawler.scraper.{ScraperResult, ScraperState}
import babylon.crawler.writer._
import babylon.crawler.output.ResultToOutput
import babylon.common.format.PageFormat.PageList

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
    pagesPerSecond: Int,
    maxAttemptsPerPage: Int
) extends Actor with ActorLogging {
    import SupervisorActor._

    val crawler: ActorRef = context.actorOf(
        Props(new CrawlerActor(pagesPerSecond, maxAttempts = maxAttemptsPerPage)),
        "crawler"
    )
    val output: ActorRef = context.actorOf(Props(new OutputActor(resultToOutput)), "output")
    val dumper: ActorRef = context.actorOf(Props(new DumperActor(writer)), "dumper")

    def receive: Receive = {
        /**
          * Initial message to start the crawling
          */
        case Start(uri, state) =>
            crawler ! StartCrawling(uri, state)

        /**
          * A page has been successfully scraped
          */
        case Scraped(result, state) =>
            log.info("Scraped {}", result.uri.toASCIIString)
            output ! AddOutput(result)

        /**
          * Crawler actor notified us that he is done
          */
        case CrawlingDone(crawlerState) =>
            logFinalState(crawlerState)
            crawler ! PoisonPill
            output ! GetOutput

        /**
          * The output is ready to be dumped
          */
        case OutputReady(pageList) =>
            log.info("Scraped a total of {} pages", pageList.size)
            output ! PoisonPill
            dumper ! Dump(pageList)

        /**
          * The dump is ready and we can shutdown the system
          */
        case DumpReady =>
            log.info("Dump ready. Goodbye!")
            dumper ! PoisonPill
            context.system.terminate()
            context.stop(self)
    }

    private def logFinalState(crawlerState: CrawlerState) = {
        log.info("Crawling Done")
        if (crawlerState.errors.nonEmpty) {
            val errorsStrings = crawlerState.errors.map { failure =>
                s"${failure.uri.toString} -> ${failure.originalException.toString}"
            }.mkString("\n")
            log.info(s"Errors (${crawlerState.errors.size}):\n{}", errorsStrings)
        }
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
    case class CrawlingDone(state: CrawlerState) extends Message
}
