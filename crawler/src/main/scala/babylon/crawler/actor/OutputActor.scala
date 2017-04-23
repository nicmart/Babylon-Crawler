package babylon.crawler.actor

import akka.actor.{Actor, ActorLogging}
import babylon.common.format.PageFormat
import babylon.crawler.actor.OutputActor.{AddOutput, GetOutput}
import babylon.crawler.actor.SupervisorActor.OutputReady
import babylon.crawler.scraper.ScraperResult
import babylon.common.format.PageFormat.PageList
import babylon.crawler.output.ResultToOutput

/**
  * This actor incrementally builds the aggregated output of the scrapers
  */
class OutputActor(resultToOutput: ResultToOutput) extends Actor with ActorLogging {
    /**
      * The initial state is the empty page list
      */
    def receive: Receive = active(PageFormat.empty)

    /**
      * Change the active state to a new page list
      */
    def active(pages: PageList): Receive = {
        case AddOutput(result) =>
            context become active(resultToOutput.fold(pages, result))
        case GetOutput =>
            sender ! OutputReady(pages)
    }
}

object OutputActor {
    sealed trait Message
    case class AddOutput(result: ScraperResult) extends Message
    case object GetOutput extends Message
}