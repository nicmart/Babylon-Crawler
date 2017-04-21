package babylon.crawler.scraper

import java.net.URI

import babylon.crawler.browser.{BrowserResponse, MapBrowser}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.{Matchers, WordSpec}

class LinkExtractorScraperStateSpec extends WordSpec with Matchers {

    "A LinkExtractorScraperState" must {
        var browser = new MapBrowser(Map.empty)
        val scraperResult = ScraperResult(
            new URI("http://test1"),
            new JsoupBrowser().parseString("hi")
        )

        "give the empty scraper when no extractors are provided" in {
            val state = new LinkExtractorsScraperState(
                browser,
                Nil
            )

            state.scraper shouldBe LinkExtractorScraper(browser, LinkExtractor.empty)
            state.next(scraperResult).scraper shouldBe LinkExtractorScraper(browser, LinkExtractor.empty)
        }

        val extractor1 = new LinkExtractor {
            def extractLinks(browserResponse: BrowserResponse): List[URI] = ???
        }
        val extractor2 = new LinkExtractor {
            def extractLinks(browserResponse: BrowserResponse): List[URI] = ???
        }

        val state = new LinkExtractorsScraperState(browser, List(extractor1, extractor2))

        "give a next state who gives the second extractor of the list" in {
            state.next(scraperResult).scraper shouldBe LinkExtractorScraper(browser, extractor2)
        }
    }

}
