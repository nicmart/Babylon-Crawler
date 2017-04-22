package babylon.crawler.actor

import java.net.URI

import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import babylon.crawler.actor.CrawlerActor.Scraped
import babylon.crawler.actor.ScraperActor.Scrape
import babylon.crawler.scraper.{ConstantScraperState, MapScraper, ScraperResult}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.reflect._

class ScraperActorSpec extends TestKit(ActorSystem("ScraperActorSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

    import ScraperActorSpec._

    "A Scraper Actor" must {

        "use the scraper in the message to scrape the URI" in {
            val uri = new URI("http://test")
            val parent = TestProbe()
            val scraper = parent.childActorOf(Props(new ScraperActor))
            scraper ! Scrape(uri, scraperState)
            parent.expectMsg(Scraped(scraperResults(uri), scraperState))
        }

        "return a failure if the scraping failed" in {
            val uri = new URI("http://test2")
            val parent = TestProbe()
            val scraper = parent.childActorOf(Props(new ScraperActor))
            scraper ! Scrape(uri, scraperState)
            parent.expectMsgType(classTag[Failure])
        }

    }

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
}

object ScraperActorSpec {
    val jsoupBrowser = new JsoupBrowser()

    val scraperResults = Map(
        new URI("http://test") -> ScraperResult(
            new URI("http://test"),
            jsoupBrowser.parseString("<html></html>"),
            Nil,
            Nil
        )
    )

    val scraper = new MapScraper(scraperResults)
    val scraperState = ConstantScraperState(scraper)

}