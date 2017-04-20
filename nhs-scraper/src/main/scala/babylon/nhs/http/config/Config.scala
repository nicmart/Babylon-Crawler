package babylon.nhs.http.config

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorRef, ActorSystem, Props}
import babylon.nhs.actor.SupervisorActor
import babylon.nhs.output.Output.PageList
import babylon.nhs.output.ResultToOutput
import babylon.nhs.scraper.ScraperState
import babylon.nhs.serialiser.{JsonCirceSerialiser, Serialiser}
import babylon.nhs.writer.{JavaFileWriter, SerialiserWriter, Writer}

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
case class Config(
    startPage: URI,
    output: File,
    serialiser: Serialiser[PageList],
    resultToOutput: ResultToOutput,
    initialState: ScraperState,
    pagesPerSecond: Int
) {
    def supervisor(system: ActorSystem): ActorRef = {
        system.actorOf(Props(new SupervisorActor(writer, resultToOutput, pagesPerSecond)), "supervisor")
    }

    def writer: Writer[PageList] = new SerialiserWriter(
        JsonCirceSerialiser,
        new JavaFileWriter(new FileWriter(output))
    )
}
