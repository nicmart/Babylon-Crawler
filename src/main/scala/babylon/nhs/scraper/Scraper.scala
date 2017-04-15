package babylon.nhs.scraper

import java.net.URI

import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by nic on 15/04/2017.
  */
trait Scraper {
    def scrape(uri: URI): Future[ScraperResult]
}

object Scraper {
    def apply(browser: Browser, linkExtractor: LinkExtractor): Scraper = new Scraper {
        override def scrape(uri: URI) = browser.get(uri).map { browserResponse =>
            ScraperResult(
                browserResponse.uri,
                browserResponse.document,
                linkExtractor.extractLinks(browserResponse)
            )
        }
    }
}

final case class ScraperResult(
    uri: URI,
    document: Document,
    links: List[URI]
)
