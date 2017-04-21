package babylon.crawler.browser

import java.net.URI

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.scalatest.{AsyncWordSpec, Matchers, WordSpec}

/**
  * Created by Nicolò Martini on 21/04/2017.
  */
class BrowserSpec extends AsyncWordSpec with Matchers {

    "A Scala Scraper based Browser" must {

        "use the inner JSoupBrowser to get the page" in {

            val jsoupBrowser = new JsoupBrowser() {
                override def get(url: String): Document = this.parseString(s"<a href='$url'></a>")
            }
            val uri = new URI("http://test")
            val expectedResponse = BrowserResponse(
                uri,
                jsoupBrowser.parseString(s"<a href='$uri'></a>")
            )

            val browser = new ScalaScraperBrowser(jsoupBrowser)

            browser.get(uri) map { response =>
                response.toString shouldBe expectedResponse.toString
            }
        }

    }

}
