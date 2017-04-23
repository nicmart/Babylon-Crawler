package babylon.crawler.output

import java.net.URI

import babylon.common.format.{PageElement, PageFormat}
import babylon.crawler.browser.BrowserResponse
import babylon.crawler.scraper.ScraperResult
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Nicolò Martini on 21/04/2017.
  */
class CssContentResultToPageFormatSpec extends WordSpec with Matchers {

    import CssContentResultToPageFormatSpec._

    "A CSS Content Extractor" must {
        "extract the content using a css selector" in {
            val scraperResult = ScraperResult(
                new URI("http://test"),
                document("<title>My Title</title><div class='foo'><div class='bar'>Content 1</div><div>Content 2</div></div>")
            )
            val expectedOutput = List(
                PageElement(
                    "http://test",
                    "My Title",
                    "Content 1",
                    List()
                )
            )

            val converter = new CssContentResultToOutput(".foo .bar")
            converter.fold(PageFormat.empty, scraperResult) shouldBe expectedOutput
        }
    }

}

object CssContentResultToPageFormatSpec {
    // Used to parse HTML string into Document objects
    def document(html: String): Document = {
        new JsoupBrowser().parseString(html)
    }
}
