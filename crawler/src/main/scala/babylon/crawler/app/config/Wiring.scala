package babylon.crawler.app.config

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.crawler.actor.SupervisorActor
import babylon.crawler.actor.SupervisorActor.Start
import babylon.crawler.browser.{RandomFailingBrowser, ScalaScraperBrowser}
import babylon.crawler.output.CssContentResultToOutput
import babylon.crawler.output.Output.PageList
import babylon.crawler.scraper.{CssSelectorLinkExtractor, LinkExtractorsScraperState}
import babylon.crawler.writer.{JavaFileWriter, JsonWriter}
import io.circe.Encoder
import io.circe.generic.auto._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

/**
  * Created by Nicolò Martini on 21/04/2017.
  */
object Wiring {
    lazy val startPage = new URI("http://www.nhs.uk/Conditions/Pages/hub.aspx")
    lazy val outputFile = new File("cache/pages.json")
    lazy val contentCssSelector = ".main-content,.article,.page-section .column--two-thirds"
    lazy val cssLinkSelectorStack = List(
        "#haz-mod1 a",
        "#ctl00_PlaceHolderMain_BodyMap_ConditionsByAlphabet a",
        "#ctl00_PlaceHolderMain_articles a"
    )
    lazy val pagesPerSecond = 5

    lazy val initialMessage = Start(startPage, initialState)

    lazy val resultToOutput = new CssContentResultToOutput(contentCssSelector)
    lazy val pageListJsonEncoder = implicitly[Encoder[PageList]]
    //lazy val browser = new RandomFailingBrowser(new ScalaScraperBrowser(new JsoupBrowser()), 0.1)
    lazy val browser = new ScalaScraperBrowser(new JsoupBrowser())
    lazy val initialState = new LinkExtractorsScraperState(
        browser,
        cssLinkSelectorStack.map(new CssSelectorLinkExtractor(_))
    )
    lazy val javaFileWriter =  new JavaFileWriter(new FileWriter(outputFile))
    lazy val writer = new JsonWriter(pageListJsonEncoder, javaFileWriter)

    lazy val actorSystem = ActorSystem("nhs-scraper")
    lazy val supervisorProps = Props(new SupervisorActor(writer, resultToOutput, pagesPerSecond))
    lazy val supervisor = actorSystem.actorOf(supervisorProps, "supervisor")
}
