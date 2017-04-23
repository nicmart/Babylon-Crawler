package babylon.crawler.browser

import java.net.URI

import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper
import scala.concurrent.{ExecutionContext, Future}

/**
  * Our definition of browser
  */
trait Browser {
    def get(uri: URI)(implicit executionContext: ExecutionContext): Future[BrowserResponse]
}

/**
  *  The response returned by the browser
  * @param uri The URI of the request
  * @param document A scalascraper document instance
  */
final case class BrowserResponse(
    uri: URI,
    document: Document
)

/**
  * An implementation of Browser that uses a generic ScalaScraper browser instance
  */
class ScalaScraperBrowser(innerBrowser: scalascraper.browser.Browser) extends Browser {
    def get(uri: URI)(implicit executionContext: ExecutionContext): Future[BrowserResponse] =
        Future {
            val jsoupResponse = innerBrowser.get(uri.toString)
            BrowserResponse(
                uri,
                innerBrowser.get(uri.toString)
            )
        }
}

/**
  * A fake browser that returns responses based on a static URI->Document map
  * Used for testing
  */
class MapBrowser(map: Map[URI, Document]) extends Browser {
    def get(uri: URI)(implicit executionContext: ExecutionContext): Future[BrowserResponse] = {
        map.get(uri) match {
            case Some(document) => Future.successful(BrowserResponse(uri, document))
            case None => Future.failed(new java.net.UnknownHostException())
        }
    }
}

/**
  * Used to increase the number of failures of a browser, for testing purpose.
  * This will make the inner browser fail with probability @param probability
  */
class RandomFailingBrowser(browser:Browser, probability: Double) extends Browser {
    def get(uri: URI)(implicit executionContext: ExecutionContext): Future[BrowserResponse] = {
        if (Math.random() <= probability) {
            Future.failed(new RuntimeException)
        } else {
            browser.get(uri)
        }
    }
}
