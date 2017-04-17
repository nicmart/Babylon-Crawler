package babylon.nhs.scraper

import java.net.URI

import babylon.nhs.browser.Browser
import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by nic on 15/04/2017.
  */
trait Scraper {
    def scrape(uri: URI): Future[ScraperResult]
}

case class LinkExtractorScraper(browser: Browser, linkExtractor: LinkExtractor) extends Scraper {
    def scrape(uri: URI): Future[ScraperResult] = {
        browser.get(uri).map { browserResponse =>
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
