package babylon.nhs.actor

import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.OutputActor.{AddOutput, GetOutput}
import babylon.nhs.actor.SupervisorActor.OutputReady
import babylon.nhs.scraper.ScraperResult
import babylon.nhs.output.Output.PageList
import babylon.nhs.output.ResultToOutput

/**
  * This actor incrementally builds the aggregated output of the scrapers
  */
class OutputActor(resultToOutput: ResultToOutput) extends Actor with ActorLogging {
    def receive: Receive = active(List.empty)
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