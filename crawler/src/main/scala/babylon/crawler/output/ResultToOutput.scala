package babylon.crawler.output

import babylon.common.format.PageElement
import babylon.common.format.PageFormat.PageList
import babylon.crawler.scraper.ScraperResult
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.allText
import net.ruippeixotog.scalascraper.dsl.DSL._

/**
  * An incremental build of a page list from ScraperResults
  */
trait ResultToOutput {
    def fold(output: PageList, result: ScraperResult): PageList
}

/**
  * A ResultToOutput that extract the content using a css selector
  * @param contentSelector CSS selector to extract the content
  */
class CssContentResultToOutput(contentSelector: String) extends ResultToOutput {
    def fold(output: PageList, result: ScraperResult): PageList = {
        PageElement(
            result.uri.toString,
            result.document.title,
            result.document >> allText(contentSelector),
            result.ancestors.map(_.toString)
        ) :: output
    }
}
