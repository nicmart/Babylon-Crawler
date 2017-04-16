package babylon.nhs.scraper

import java.net.URI

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

import scala.util.Try

/**
  * Created by nic on 15/04/2017.
  */
trait LinkExtractor {
    def extractLinks(browserResponse: BrowserResponse): List[URI]
}

object LinkExtractor {
    val empty: LinkExtractor = new LinkExtractor {
        override def extractLinks(browserResponse: BrowserResponse) = Nil
    }
}

/**
  * Extract links href selecting the links with a css selector
  */
class CssSelectorLinkExtractor(selector: String) extends LinkExtractor {

    override def extractLinks(browserResponse: BrowserResponse): List[URI] = {
        val baseURI = browserResponse.uri
        val document = browserResponse.document
        val links = document >> elementList(selector)
        links.flatMap { link =>
            val tryAbsoluteUri = Try { baseURI.resolve(link.attr("href").trim) }
            tryAbsoluteUri.map(List(_)).getOrElse(Nil)
        }.take(3)
    }
}
