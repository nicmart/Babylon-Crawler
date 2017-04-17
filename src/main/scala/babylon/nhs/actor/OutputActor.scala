package babylon.nhs.actor

import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.OutputActor.{AddOutput, GetOutput}
import babylon.nhs.actor.SupervisorActor.OutputReady
import babylon.nhs.scraper.ScraperResult
import babylon.nhs.output.Output.PageList
import babylon.nhs.output.ResultToOutput
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
/**
  * Created by nic on 15/04/2017.
  */
class OutputActor(resultToOutput: ResultToOutput) extends Actor with ActorLogging {
    def receive: Receive = active(List.empty)
    def active(pages: PageList): Receive = {
        case AddOutput(result) =>
            log.info("Storing result for {}", result.uri.toString)
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