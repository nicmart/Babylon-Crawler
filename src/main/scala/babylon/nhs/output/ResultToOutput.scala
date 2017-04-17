package babylon.nhs.output

import babylon.nhs.output.Output.PageList
import babylon.nhs.scraper.ScraperResult
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.allText
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
trait ResultToOutput {
    def fold(output: PageList, result: ScraperResult): PageList
}

class CssContentResultToOutput(contentSelector: String) extends ResultToOutput {
    def fold(output: PageList, result: ScraperResult): PageList = {
        PageElement(
            result.uri.toString,
            result.document.title,
            result.document >> allText(contentSelector),
            result.path.map(_.toString)
        ) :: output
    }
}
