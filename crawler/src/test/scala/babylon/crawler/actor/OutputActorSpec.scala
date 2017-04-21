package babylon.crawler.actor

import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import babylon.crawler.actor.OutputActor.{AddOutput, GetOutput}
import babylon.crawler.actor.SupervisorActor.OutputReady
import babylon.crawler.output.Output.PageList
import babylon.crawler.output.{PageElement, ResultToOutput}
import babylon.crawler.scraper.ScraperResult
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.{Document, Element}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
class OutputActorSpec extends TestKit(ActorSystem("OutputActorSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

    import OutputActorSpec._

    "OutputActor" must {
        "translate to the correct output" in {
            val outputActor = system.actorOf(Props(new OutputActor(resultToOutput)))

            for (result <- scraperResults) {
                outputActor ! AddOutput(result)
            }

            outputActor ! GetOutput

            expectMsg(OutputReady(expectedOutput))
        }
    }

}

object OutputActorSpec {
    def document(html: String): Document = {
        new JsoupBrowser().parseString(html)
    }

    def scraperResults: List[ScraperResult] = List(
        ScraperResult(
            new URI("http://test1"),
            document("<html></html>"),
            List(),
            List()
        ),
        ScraperResult(
            new URI("http://test2"),
            document("<html></html>"),
            List(),
            List()
        )
    )

    def expectedOutput: PageList = List(
        PageElement(
            "http://test2",
            "http://test2",
            "http://test2",
            Nil
        ),
        PageElement(
            "http://test1",
            "http://test1",
            "http://test1",
            Nil
        )
    )

    def resultToOutput: ResultToOutput = new ResultToOutput {
        def fold(output: PageList, result: ScraperResult): PageList = PageElement(
            result.uri.toString,
            result.uri.toString,
            result.uri.toString,
            List()
        ) :: output
    }
}
