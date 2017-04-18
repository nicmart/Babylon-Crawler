package babylon.nhs.app

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.nhs.actor.SupervisorActor.Start
import babylon.nhs.actor.SupervisorActor
import babylon.nhs.browser.{Browser, ScalaScraperBrowser}
import babylon.nhs.output.CssContentResultToOutput
import babylon.nhs.output.Output.PageList
import babylon.nhs.scraper._
import babylon.nhs.serialiser.JsonCirceSerialiser
import babylon.nhs.writer.{JavaFileWriter, SerialiserWriter, Writer}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

/**
  * Created by nic on 13/04/2017.
  */
object ScraperApp extends App {

    //val url = new URI("http://wwww.nhs.uk/Conditions/Pages/hub.aspx")
    val url = new URI("http://wwww.ssssssssssssssssssssnhs.uk/Conditions/Pages/hub.aspx")
    val filename = "pages.json"
    val browser = new ScalaScraperBrowser(new JsoupBrowser())
    val scraperState = new LinkExtractorsScraperState(browser, List(
        new CssSelectorLinkExtractor("#haz-mod1 a"),
        new CssSelectorLinkExtractor("#ctl00_PlaceHolderMain_BodyMap_ConditionsByAlphabet a")
    ))
    val writer: Writer[PageList] = new SerialiserWriter(
        JsonCirceSerialiser,
        new JavaFileWriter(new FileWriter(new File(filename)))
    )
    val resultToOutput = new CssContentResultToOutput(".main-content,.article,.page-section .column--two-thirds")
    val system = ActorSystem("nhs-scraper")
    val supervisor = system.actorOf(Props(new SupervisorActor(writer, resultToOutput)), "supervisor")

    supervisor ! Start(url, scraperState)
}
