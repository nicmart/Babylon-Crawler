package babylon.nhs.actor

import java.net.URI
import java.util.concurrent.Executor

import akka.actor.Actor.Receive
import akka.pattern.pipe
import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.ScraperActor.Scrape
import babylon.nhs.scraper.{Scraper, ScraperResult, ScraperState}

import scala.concurrent.ExecutionContext

/**
  * Created by nic on 15/04/2017.
  */
class ScraperActor extends Actor with ActorLogging {
    implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

    override def receive: Receive = {
        case Scrape(uri, state) =>
            log.info("ScraperActor scraping {}", uri.toString)
            state.scraper.scrape(uri, state).map(CrawlerActor.Scraped(_, state)) pipeTo context.parent
            context.stop(self)
    }
}

object ScraperActor {
    sealed trait Message
    case class Scrape(uri: URI, state: ScraperState) extends Message
}
