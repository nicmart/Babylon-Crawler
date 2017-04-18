package babylon.nhs.actor

import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import babylon.nhs.actor.CrawlerActor.StartCrawling
import babylon.nhs.actor.ScraperActor.Scrape
import babylon.nhs.actor.SupervisorActor.{CrawlingDone, Scraped}
import babylon.nhs.scraper.{ConstantScraperState, MapScraper, ScraperResult}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class CrawlerActorSpec extends TestKit(ActorSystem("CrawlerActorSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

    import CrawlerActorSpec._

    "A Crawler Actor" must {

        "follows all the urls in the scraper results" in {
            val uri = new URI("http://test1")
            val parent = TestProbe()
            val crawler = parent.childActorOf(Props(new CrawlerActor(100, 100.seconds)))
            crawler ! StartCrawling(uri, scraperState)

            val expectedMessages = scraperResults.values.map { result =>
                Scraped(result, scraperState)
            }.toList

            parent.expectMsgAllOf(expectedMessages: _*)
            parent.expectMsg(CrawlingDone)
        }

        "timeout if some links are not found" in {
            val uri = new URI("http://wrong")
            val parent = TestProbe()
            val scraper = parent.childActorOf(Props(new CrawlerActor(100, 1.seconds)))
            scraper ! Scrape(uri, scraperState)
            parent.expectMsg(2.seconds, CrawlingDone)
        }

    }
}

object CrawlerActorSpec {
    val jsoupBrowser = new JsoupBrowser()

    val scraperResults = Map(
        new URI("http://test1") -> ScraperResult(
            new URI("http://test1"),
            jsoupBrowser.parseString("<html></html>"),
            List(new URI("http://test2"), new URI("http://test3")),
            Nil
        ),
        new URI("http://test2") -> ScraperResult(
            new URI("http://test2"),
            jsoupBrowser.parseString("<html></html>"),
            Nil,
            Nil
        ),
        new URI("http://test3") -> ScraperResult(
            new URI("http://test3"),
            jsoupBrowser.parseString("<html></html>"),
            Nil,
            Nil
        )
    )

    val scraper = new MapScraper(scraperResults)
    val scraperState = ConstantScraperState(scraper)

}