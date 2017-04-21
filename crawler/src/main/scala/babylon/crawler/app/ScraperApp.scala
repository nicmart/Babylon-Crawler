package babylon.search.app

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.crawler.actor.SupervisorActor.Start
import babylon.crawler.actor.SupervisorActor
import babylon.search.app.config.DefaultConfig
import babylon.crawler.browser.{Browser, ScalaScraperBrowser}
import babylon.crawler.output.CssContentResultToOutput
import babylon.crawler.output.Output.PageList
import babylon.crawler.scraper._
import babylon.crawler.serialiser.JsonCirceSerialiser
import babylon.crawler.writer.{JavaFileWriter, SerialiserWriter, Writer}
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
