package babylon.nhs.scraper

import java.net.URI

import babylon.nhs.browser.Browser

trait ScraperState {
    def scraper: Scraper
    def path: List[URI]
    def next(result: ScraperResult): ScraperState
    def isLeaf: Boolean
}

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
        new LinkExtractorsScraperState(browser, linkExtractors.tail, result.uri :: result.path)
}