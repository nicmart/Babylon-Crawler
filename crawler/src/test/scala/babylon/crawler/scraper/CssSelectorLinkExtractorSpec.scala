package babylon.crawler.scraper

import java.net.URI

import babylon.crawler.browser.BrowserResponse
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Nicolò Martini on 21/04/2017.
  */
class CssSelectorLinkExtractorSpec extends WordSpec with Matchers {

    import CssSelectorLinkExtractorSpec._

    "A CSS Link Extractor" must {
        "extract links using a css selector" in {

            val browserResponse = BrowserResponse(
                new URI("http://test"),
                document("<div class='a'><div class='b'><a href='http://test2'></a></div><a href='http://test3'></a></div>")
            )

            val extractor = new CssSelectorLinkExtractor(".a .b a")
            val expected = List(new URI("http://test2"))
            extractor.extractLinks(browserResponse) shouldBe expected
        }
    }
}

object CssSelectorLinkExtractorSpec {
    // Used to parse HTML string into Document objects
    def document(html: String): Document = {
        new JsoupBrowser().parseString(html)
    }
}
