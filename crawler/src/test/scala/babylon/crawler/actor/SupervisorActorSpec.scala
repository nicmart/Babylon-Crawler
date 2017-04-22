package babylon.crawler.actor

import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import babylon.crawler.actor.SupervisorActor.{DumpReady, Start}
import babylon.crawler.output.Output.PageList
import babylon.crawler.scraper.ScraperResult
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SupervisorActorSpec extends TestKit(ActorSystem("SupervisorActorSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

    import SupervisorActorSpec._

    "A Supervisor Actor" must {

        "dump the final output" in {
            val uri = new URI("http://test1")
            val parent = TestProbe()

            var output: PageList = null

            val writer = DumperActorSpec.writer { output = _ }

            val supervisor = parent.childActorOf(Props(new SupervisorActor(writer, resultToOutput, 10)))
            parent.watch(supervisor)
            supervisor ! Start(uri, scraperState)
            parent.expectTerminated(supervisor)

            output.toSet shouldBe expectedOutput.toSet
        }
    }

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
}

object SupervisorActorSpec {
    val resultToOutput = OutputActorSpec.resultToOutput
    val scraperState = CrawlerActorSpec.scraperState
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

    val expectedOutput: PageList = scraperResults.values.foldLeft[PageList](Nil) {
        case (pageList, scraperResult) => resultToOutput.fold(pageList, scraperResult)
    }
}