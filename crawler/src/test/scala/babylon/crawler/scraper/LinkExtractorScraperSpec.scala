package babylon.crawler.scraper

import java.net.URI

import babylon.crawler.browser.{BrowserResponse, MapBrowser}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.{AsyncWordSpec, Matchers, WordSpec}

class LinkExtractorScraperSpec extends AsyncWordSpec with Matchers {

    import LinkExtractorScraperSpec._

    "A link extractor scraper" must {

        "usa a browser and a link extractor to scrape a page" in {

            val scraper = LinkExtractorScraper(browser, extractor)

            scraper.scrape(new URI("http://test1"), ConstantScraperState(scraper)).map { scraperResult =>
                scraperResult.document.title shouldBe "Hi"
            }
        }

    }
}

object LinkExtractorScraperSpec {

    val browser = new MapBrowser(Map(
        new URI("http://test1") -> new JsoupBrowser().parseString("<title>Hi</title>")
    ))

    val extractor = new LinkExtractor {
        def extractLinks(browserResponse: BrowserResponse): List[URI] = browserResponse.uri.toString match {
            case "http://test1" => List(new URI("http://test2"))
            case _ => List.empty
        }
    }
}
