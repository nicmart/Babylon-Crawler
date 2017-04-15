package babylon.nhs.app

import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.nhs.actor.Controller
import Controller._
import babylon.nhs.scraper._

/**
  * Created by nic on 13/04/2017.
  */
object ScraperApp extends App {

    val url = new URI("http://www.nhs.uk/Conditions/Pages/hub.aspx")

    val scraperState = new FromLinkExtractorsScraperState(new Browser, List(
        //new CssSelectorLinkExtractor("#haz-mod1 a"),#haz-mod1 > ul > li:nth-child(1) > a
        new CssSelectorLinkExtractor("#haz-mod1 > ul > li:nth-child(1) > a"),
        new CssSelectorLinkExtractor("#ctl00_PlaceHolderMain_BodyMap_ConditionsByAlphabet a")
    ))

    val system = ActorSystem("nhs-scraper")
    val controller = system.actorOf(Props(new Controller), "controller")

    controller ! Scrape(url, scraperState)
}
