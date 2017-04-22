package babylon.crawler.actor.state

import java.net.URI

import akka.actor.ActorRef
import babylon.crawler.scraper.Scraper.ScraperFailure

/**
  * This is the immutable state of the crawler.
  * It consists of a map of urls -> scrapers, and a set of visited links
  */
final case class CrawlerState(
    activeScrapers: Map[String, ActorRef] = Map.empty,
    pendingLinks: Map[String, Int] = Map.empty.withDefaultValue(0),
    visitedLinks: Set[String] = Set.empty,
    errors: List[ScraperFailure] = List.empty
) {
    /**
      * Add a new active scraper
      */
    def addScraper(scraper: ActorRef, uri: URI): CrawlerState = {
        val normalisedUri = normaliseUri(uri)
        copy(
            activeScrapers = activeScrapers + (normaliseUri(uri) -> scraper),
            pendingLinks = pendingLinks.updated(
                normaliseUri(uri),
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
            activeScrapers = activeScrapers - normalisedUri,
            pendingLinks = pendingLinks.updated(normalisedUri, 0)
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
      * Add an error to the state
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

    private def normaliseUri(uri: URI): String = uri.toString.toLowerCase
}
