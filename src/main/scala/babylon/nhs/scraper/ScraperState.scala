package babylon.nhs.scraper

import java.net.URI

import babylon.nhs.browser.Browser

/**
  * The scraper state is used to evolve the scraping strategy during the scraping
  * Given a ScraperResult, the next method gives the next state for scraping subpages of the pages
  * scraped by ScraperResult
  */
trait ScraperState {
    def next(result: ScraperResult): ScraperState
    def scraper: Scraper
    def path: List[URI]
    def isLeaf: Boolean
}

/**
  * Fetch web pages using a Browser and extract links using a stack of LinkExtractors.
  * The next state will an instance of the same class with a popped link extractors stack
  */
class LinkExtractorsScraperState(
    browser: Browser,
    extractors: List[LinkExtractor],
    val path: List[URI] = Nil
) extends ScraperState {
    private val linkExtractors: List[LinkExtractor] = extractors match {
        case Nil => List(LinkExtractor.empty)
        case _ => extractors
    }

    def scraper: Scraper = LinkExtractorScraper(browser, linkExtractors.head)
    def isLeaf: Boolean = extractors.isEmpty
    def next(result: ScraperResult): ScraperState =
        new LinkExtractorsScraperState(browser, linkExtractors.tail, result.uri :: result.ancestors)
}

/**
  * A Scraper state that stays always the same
  * Mainly used for testing
  */
case class ConstantScraperState(
    scraper: Scraper,
    path: List[URI] = Nil,
    isLeaf: Boolean = true
) extends ScraperState {
    def next(result: ScraperResult): ScraperState = this
}