package babylon.crawler.app.config

import java.io.{File, FileWriter}
import java.net.URI

import akka.actor.{ActorSystem, Props}
import babylon.crawler.actor.SupervisorActor
import babylon.crawler.actor.SupervisorActor.Start
import babylon.crawler.browser.ScalaScraperBrowser
import babylon.crawler.output.CssContentResultToOutput
import babylon.common.format.PageFormat.PageList
import babylon.crawler.scraper.{CssSelectorLinkExtractor, LinkExtractorsScraperState}
import babylon.crawler.writer.{JavaFileWriter, JsonWriter}
import io.circe.Encoder
import io.circe.generic.auto._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

trait Wiring {
    /**
      * Config parameters
      */
    lazy val startPage = new URI("http://www.nhs.uk/Conditions/Pages/hub.aspx")
    lazy val outputFile = new File("cache/pages.json")
    lazy val contentCssSelector = ".main-content,.article,.page-section .column--two-thirds"
    lazy val cssLinkSelectorStack = List(
        "#haz-mod1 a",
        "#ctl00_PlaceHolderMain_BodyMap_ConditionsByAlphabet a",
        "#ctl00_PlaceHolderMain_articles a"
    )
    lazy val maxPagesPerSecond = 5
    lazy val maxAttemptsPerPage = 4

    /**
      * Components
      */
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
    lazy val supervisorProps = Props(new SupervisorActor(writer, resultToOutput, maxPagesPerSecond, maxAttemptsPerPage))
    lazy val supervisor = actorSystem.actorOf(supervisorProps, "supervisor")
}

object Wiring extends Wiring
