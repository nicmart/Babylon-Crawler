package babylon.nhs.app.config

import java.io.File
import java.net.URI

import babylon.nhs.browser.ScalaScraperBrowser
import babylon.nhs.config.Config
import babylon.nhs.output.CssContentResultToOutput
import babylon.nhs.scraper.{CssSelectorLinkExtractor, LinkExtractorsScraperState}
import babylon.nhs.serialiser.JsonCirceSerialiser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
object DefaultConfig {
    def apply() = Config(
        startPage = new URI("http://www.nhs.uk/Conditions/Pages/hub.aspx"),
        output = new File("cache/pages.json"),
        serialiser = JsonCirceSerialiser,
        resultToOutput =  new CssContentResultToOutput(
            // This selector extracts the content of the page
            ".main-content,.article,.page-section .column--two-thirds"
        ),
        initialState = new LinkExtractorsScraperState(
            new ScalaScraperBrowser(new JsoupBrowser()),
            List(
                // Select links in initial page
                new CssSelectorLinkExtractor("#haz-mod1 a"),
                // Select links in "letter" pages
                new CssSelectorLinkExtractor("#ctl00_PlaceHolderMain_BodyMap_ConditionsByAlphabet a")
            )
        ),
        // Maximum number of Scrape requests per second.
        // Increase this to avoid api rates limits
        pagesPerSecond = 10
    )
}
