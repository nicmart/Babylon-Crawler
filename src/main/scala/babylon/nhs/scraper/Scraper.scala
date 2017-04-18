package babylon.nhs.scraper

import java.net.URI

import babylon.nhs.browser.Browser
import babylon.nhs.scraper.Scraper.ScraperFailure
import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * This is the component that does the scraping.
  * The scraping is done asynchronously, returning a Future that will hold a ScraperResult
  */
trait Scraper {
    def scrape(uri: URI, state: ScraperState): Future[ScraperResult]
}

object Scraper {
    case class ScraperFailure(uri: URI, state: ScraperState, originalException: Throwable) extends RuntimeException
}

/**
  * A Scraper implementation based on a Browser and LinkExtractor instances
  */
case class LinkExtractorScraper(browser: Browser, linkExtractor: LinkExtractor) extends Scraper {
    def scrape(uri: URI, state: ScraperState): Future[ScraperResult] = {
        browser.get(uri).transform {
            case Success(browserResponse) => Success(
                ScraperResult(
                    browserResponse.uri,
                    browserResponse.document,
                    linkExtractor.extractLinks(browserResponse),
                    state.path
                )
            )
            case Failure(exception) => Failure(new ScraperFailure(uri, state, exception))
        }
    }
}

/**
  * Implementation based on a static map
  * Mainly used for test purposes
  */
class MapScraper(map: Map[URI, ScraperResult]) extends Scraper {
    def scrape(uri: URI, state: ScraperState): Future[ScraperResult] = {
        map.get(uri) match {
            case Some(scraperResult) => Future.successful(scraperResult)
            case None => Future.failed(new NoSuchElementException)
        }
    }
}

/**
  * Result Type of the scrapers.
  * @param uri The uri scraped
  * @param document The content of the page
  * @param links The links extracted from the page
  * @param ancestors The list of ancestors
  */
final case class ScraperResult(
    uri: URI,
    document: Document,
    links: List[URI],
    ancestors: List[URI]
)
