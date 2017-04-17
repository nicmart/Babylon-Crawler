package babylon.nhs.browser

import java.net.URI

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

/**
  * Created by nic on 13/04/2017.
  */
class Browser {
    val browser = new JsoupBrowser()
    def get(uri: URI)(implicit executionContext: ExecutionContext): Future[BrowserResponse] =
        Future {
            val jsoupResponse = browser.get(uri.toString)
            BrowserResponse(
                uri,
                browser.get(uri.toString)
            )
        }
}

final case class BrowserResponse(
    uri: URI,
    document: Document
)
