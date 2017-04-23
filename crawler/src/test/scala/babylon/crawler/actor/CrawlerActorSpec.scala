package babylon.crawler.actor

import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import babylon.crawler.actor.CrawlerActor.StartCrawling
import babylon.crawler.actor.SupervisorActor.{CrawlingDone, Scraped}
import babylon.crawler.scraper._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
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
            parent.expectMsgType[CrawlingDone]
        }

        "end before timeout even if some links were not found" in {
            val uri = new URI("http://wrong")
            val parent = TestProbe()
            val crawler = parent.childActorOf(Props(new CrawlerActor(100, 10.seconds, 3)))
            crawler ! StartCrawling(uri, scraperState)
            parent.expectMsgType[CrawlingDone]
        }

        "collect scraping errors" in {
            val uri = new URI("http://wrong")
            val parent = TestProbe()
            val crawler = parent.childActorOf(Props(new CrawlerActor(100, 10.seconds)))
            crawler ! StartCrawling(uri, scraperState)
            parent.expectMsgPF() {
                // We expect no errors
                case CrawlingDone(crawlerState) if crawlerState.errors.nonEmpty  => true
            }
        }

        "reschedule scraping in case of failures" in {
            val uri = new URI("http://test1")
            val parent = TestProbe()
            val crawler = parent.childActorOf(Props(new CrawlerActor(100, 10.seconds, maxAttempts = 3)))
            crawler ! StartCrawling(uri, ConstantScraperState(scraperFailingAtFirst2Tries))
            parent.expectMsgType[Scraped]
            parent.expectMsgType[Scraped]
            parent.expectMsgType[Scraped]
            parent.expectMsgType[CrawlingDone]
        }

        "not try to scrape an URI more than maxAttempts times" in {
            val uri = new URI("http://test1")
            val parent = TestProbe()
            val crawler = parent.childActorOf(Props(new CrawlerActor(100, 10.seconds, maxAttempts = 2)))
            crawler ! StartCrawling(uri, ConstantScraperState(scraperFailingAtFirst2Tries))
            parent.expectMsgType[CrawlingDone]
        }
    }

    override def afterAll {
        TestKit.shutdownActorSystem(system)
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

    def scraperStack(scrapers: Iterable[Scraper]) = new Scraper {
        assert(scrapers.nonEmpty)
        var scraperStack = scrapers
        def scrape(uri: URI, state: ScraperState): Future[ScraperResult] = {
            val current = scraperStack.head
            if (scraperStack.size > 1) {
                scraperStack = scraperStack.tail
            }
            current.scrape(uri, state)
        }
    }

    def scraperFailingAtFirst2Tries = scraperStack(List(
        new MapScraper(Map.empty),
        new MapScraper(Map.empty),
        scraper
    ))
}