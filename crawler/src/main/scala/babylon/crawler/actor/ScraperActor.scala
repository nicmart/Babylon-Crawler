package babylon.crawler.actor

import java.net.URI
import java.util.concurrent.Executor

import akka.actor.Actor.Receive
import akka.pattern.pipe
import akka.actor.{Actor, ActorLogging}
import babylon.crawler.actor.ScraperActor.Scrape
import babylon.crawler.scraper.{Scraper, ScraperResult, ScraperState}

import scala.concurrent.ExecutionContext

/**
  * The actor actually executing the scraping.
  * The actor receives the scraper inside the scraper state, in the message body.
  */
class ScraperActor extends Actor with ActorLogging {
    implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

    def receive: Receive = {
        case Scrape(uri, state) =>
            log.info("ScraperActor scraping {}", uri.toString)
            state.scraper.scrape(uri, state).map(CrawlerActor.Scraped(_, state)) pipeTo context.parent
            context.stop(self)
    }
}

object ScraperActor {
    sealed trait Message

    /**
      * A request to scrape an URI.
      *
      * @param uri The uri to scrape
      * @param state The scraper state
      */
    case class Scrape(uri: URI, state: ScraperState) extends Message
}
