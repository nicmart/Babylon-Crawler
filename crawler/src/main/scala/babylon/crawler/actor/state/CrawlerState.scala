package babylon.crawler.actor.state

import java.net.URI

import akka.actor.ActorRef
import babylon.crawler.scraper.Scraper.ScraperFailure

/**
  * This is the immutable state class used by the Crawler Actor
  * All the URI received are normalised to lowercase.
  *
  * @param activeScrapers The scrapers currently active, indexed by url
  * @param pendingLinks Keep track of the number of scraping attempts per link
  * @param visitedLinks Keep track of the visited link
  * @param errors The list of errors collected
  */
final case class CrawlerState(
    activeScrapers: Map[String, ActorRef] = Map.empty,
    pendingLinks: Map[String, Int] = Map.empty,
    visitedLinks: Set[String] = Set.empty,
    errors: List[ScraperFailure] = List.empty
) {
    /**
      * Add a new scraper actor
      */
    def addScraper(scraper: ActorRef, uri: URI): CrawlerState = {
        val normalisedUri = normaliseUri(uri)
        copy(
            activeScrapers = activeScrapers + (normalisedUri -> scraper),
            pendingLinks = pendingLinks.updated(
                normalisedUri,
                pendingLinks.getOrElse(normalisedUri, 0) + 1
            )
        )
    }

    /**
      * Mark a uri as visited
      */
    def visit(uri: URI): CrawlerState = {
        val normalisedUri = normaliseUri(uri)
        copy(
            visitedLinks = visitedLinks + normalisedUri,
            activeScrapers = activeScrapers - normalisedUri
        )
    }

    /**
      * Failed to scrape a uri
      */
    def failed(uri: URI): CrawlerState = {
        val normalisedUri = normaliseUri(uri)
        copy(activeScrapers = activeScrapers - normalisedUri)
    }

    /**
      * Add an error
      */
    def addError(scraperFailure: ScraperFailure): CrawlerState = {
        copy(errors = scraperFailure :: errors)
    }

    /**
      * Check if we already got that uri
      */
    def isNew(uri: URI): Boolean = {
        val normalisedUri = normaliseUri(uri)
        !visitedLinks.contains(normalisedUri) && !activeScrapers.contains(normalisedUri)
    }

    /**
      * When there are no active scrapers, it means we are done
      */
    def isFinal: Boolean = activeScrapers.isEmpty

    /**
      * How many scrape attempts we have performed so far
      */
    def attempts(uri: URI): Int = pendingLinks.getOrElse(normaliseUri(uri), 0)

    /**
      * Often links are case insensitive and written in different versions.
      * Here we try to normalise them.
      */
    private def normaliseUri(uri: URI): String = uri.toString.toLowerCase
}
