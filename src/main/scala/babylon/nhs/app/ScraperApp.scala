package babylon.nhs.app

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.nhs.actor.Supervisor.Start
import babylon.nhs.actor.Supervisor
import babylon.nhs.browser.Browser
import babylon.nhs.output.Output.PageList
import babylon.nhs.scraper._
import babylon.nhs.serialiser.JsonStorageSerialiser
import babylon.nhs.writer.{JavaFileWriter, SerialiserWriter, Writer}

/**
  * Created by nic on 13/04/2017.
  */
object ScraperApp extends App {

    val url = new URI("http://www.nhs.uk/Conditions/Pages/hub.aspx")
    val filename = "pages.json"

    val scraperState = new LinkExtractorsScraperState(new Browser, List(
        new CssSelectorLinkExtractor("#haz-mod1 a"),
        new CssSelectorLinkExtractor("#ctl00_PlaceHolderMain_BodyMap_ConditionsByAlphabet a")
    ))

    val writer: Writer[PageList] = new SerialiserWriter(
        JsonStorageSerialiser,
        new JavaFileWriter(new FileWriter(new File(filename)))
    )

    val system = ActorSystem("nhs-scraper")
    val supervisor = system.actorOf(Props(new Supervisor(writer)), "supervisor")

    supervisor ! Start(url, scraperState)
}
