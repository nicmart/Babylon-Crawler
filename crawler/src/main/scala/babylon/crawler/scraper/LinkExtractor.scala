package babylon.crawler.scraper

import java.net.URI

import babylon.crawler.browser.BrowserResponse
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

import scala.util.Try

/**
  * Extract a list of links from a browser response
  */
trait LinkExtractor {
    /**
      * Extract a list of URI from a page
      */
    def extractLinks(browserResponse: BrowserResponse): List[URI]

    /**
      * Limit the links returned by this extractor
      */
    def limited(n: Int): LinkExtractor = LinkExtractor.limited(this, n)
}

object LinkExtractor {
    /**
      * A link extractor that always returns the same list of links
      */
    def constant(links: List[URI]): LinkExtractor = new LinkExtractor {
        def extractLinks(browserResponse: BrowserResponse): List[URI] = links
    }

    /**
      * Limit the maximum number of links returned by a linkExtractor
      */
    def limited(linkExtractor: LinkExtractor, limit: Int) = new LinkExtractor {
        def extractLinks(browserResponse: BrowserResponse): List[URI] =
            linkExtractor.extractLinks(browserResponse).take(limit)
    }

    /**
      * A link extractor that always returns an empty list of links
      */
    val empty: LinkExtractor = constant(Nil)
}

/**
  * This extractor builds the list of links using a css selector, and extracting the href
  * attribute of each element that matches the selector.
  * If a selected element does not contain an href attribute, it discards it.
  */
class CssSelectorLinkExtractor(selector: String) extends LinkExtractor {

    override def extractLinks(browserResponse: BrowserResponse): List[URI] = {
        val baseURI = browserResponse.uri
        val document = browserResponse.document
        val links = document >> elementList(selector)
        links.flatMap { link =>
            val tryAbsoluteUri = Try { baseURI.resolve(link.attr("href").trim).normalize() }
            tryAbsoluteUri.map(List(_)).getOrElse(Nil)
        }
    }
}
