package babylon.search.http

import java.net.URLEncoder

import babylon.common.format.PageElement
import babylon.common.repository.{ByListPageElementRepository, PageElementRepository}
import babylon.search.app.config.Wiring
import io.circe.Json
import org.http4s._
import org.http4s.dsl._
import io.circe.parser._
import org.scalatest.{Matchers, WordSpec}

/**
  * This is an integration test for the whole stack
  * Only the page repository is mocked with a predefined list of pages
  * (see companion object)
  */
class RestServiceSpec extends WordSpec with Matchers {
    import RestServiceSpec._
    TestWiring.indexInitialiser.apply()

    "A Rest Service" must {
        "return the most relevant result" when {
            "we query for 'cancer'" in {
                expectFirst(
                    "cancer",
                    "Cancer - NHS Choices"
                )
            }
            "we query for 'what are the symptoms of cancer?'" in {
                expectFirst(
                    "what are the symptoms of cancer?",
                    "Cancer - Signs and symptoms - NHS Choices"
                )
            }
            "we query for 'breasts cancers'" in {
                expectFirst(
                    "breasts cancers",
                    "Breast cancer (female) - NHS Choices"
                )
            }
        }
    }

    def expectFirst(query: String, titleOfFirst: String) = {
        val response = service.run(request(query)).run
        val jsonResp = json(response)
        val title = jsonResp.hcursor
            .downField("results")
            .downN(0)
            .get[String]("title")
            .right.getOrElse("")
        title shouldBe titleOfFirst
    }
}

object RestServiceSpec {



    def request(query: String): org.http4s.Request = Request(
        method = Method.GET,
        uri = Uri.fromString("/search?query=" + URLEncoder.encode(query)).getOrElse(uri("/"))
    )

    def json(response: Response): Json =
        response.bodyAsText.map(resp => parse(resp).right.get).runLast.run.get

    lazy val service = TestWiring.httpService
    lazy val pageRepository = new ByListPageElementRepository(List(
        PageElement(
            "http://www.nhs.uk/Conditions/Cancer/Pages/Symptoms.aspx",
            "Cancer - Signs and symptoms - NHS Choices",
            ""
        ),
        PageElement(
            "http://www.nhs.uk/conditions/Cancer",
            "Cancer - NHS Choices",
            ""
        ),
        PageElement(
            "http://www.nhs.uk/conditions/Cancer-of-the-breast-female",
            "Breast cancer (female) - NHS Choices",
            ""
        )
    ))

    object TestWiring extends Wiring {
        override lazy val pageElementRepository: PageElementRepository = pageRepository
    }
}
