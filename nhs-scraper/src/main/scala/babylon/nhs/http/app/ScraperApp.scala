package babylon.nhs.http.app

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.nhs.actor.SupervisorActor.Start
import babylon.nhs.actor.SupervisorActor
import babylon.nhs.http.app.config.DefaultConfig
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

    val system = ActorSystem("nhs-scraper")
    val config = DefaultConfig()
    val supervisor = config.supervisor(system)

    supervisor ! Start(config.startPage, config.initialState)
}
