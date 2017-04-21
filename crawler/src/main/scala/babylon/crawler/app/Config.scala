package babylon.crawler.app

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorRef, ActorSystem, Props}
import babylon.crawler.actor.SupervisorActor
import babylon.crawler.output.Output.PageList
import babylon.crawler.output.ResultToOutput
import babylon.crawler.scraper.ScraperState
import babylon.crawler.serialiser.{JsonCirceSerialiser, Serialiser}
import babylon.crawler.writer.{JavaFileWriter, SerialiserWriter, Writer}

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
