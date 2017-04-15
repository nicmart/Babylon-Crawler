package babylon.nhs.scraper

/**
  * Created by nic on 15/04/2017.
  */
trait ScraperState {
    def scraper: Scraper
    def depth: Int
    def next: ScraperState
    def isLeaf: Boolean
}

class FromLinkExtractorsScraperState(
    browser: Browser,
    extractors: List[LinkExtractor],
    val depth: Int = 0
) extends ScraperState {
    private val linkExtractors: List[LinkExtractor] = extractors match {
        case Nil => List(LinkExtractor.empty)
        case _ => extractors
    }

    def scraper: Scraper = Scraper(browser, linkExtractors.head)

    def isLeaf: Boolean = extractors.isEmpty

    def next: ScraperState =
        new FromLinkExtractorsScraperState(browser, linkExtractors.tail, depth + 1)
}