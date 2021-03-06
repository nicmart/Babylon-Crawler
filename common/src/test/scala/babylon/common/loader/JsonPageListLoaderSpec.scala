package babylon.common.loader

import babylon.common.format.PageElement
import babylon.common.format.PageFormat.PageList
import babylon.common.loader.PageListLoader.PageListLoaderFailure
import io.circe.{Decoder, DecodingFailure, ParsingFailure}
import io.circe.generic.auto._
import org.scalatest.{Matchers, WordSpec}


class JsonPageListLoaderSpec extends WordSpec with Matchers {
    import JsonPageListLoaderSpec._
    "A JSON Page List Loader" must {
        "fail if the json is malformed" in {
            val loader = new JsonPageListLoader(decoder, malformedJsonSource)
            loader.load() should matchPattern {
                case Left(PageListLoaderFailure(ParsingFailure(_, _))) =>
            }
        }
        "fail if the json decoding fails" in {
            val loader = new JsonPageListLoader(decoder, invalidJsonSource)
            loader.load() should matchPattern {
                case Left(PageListLoaderFailure(failure: DecodingFailure)) =>
            }
        }
        "return a valid page list" in {
            val loader = new JsonPageListLoader(decoder, validJsonSource)

            loader.load() should matchPattern {
                case Right(pageList) if pageList == pageListForValidJson =>
            }
        }
    }
}

object JsonPageListLoaderSpec {
    val decoder = implicitly[Decoder[PageList]]
    val malformedJsonSource = scala.io.Source.fromString("{{}")
    val invalidJsonSource = scala.io.Source.fromString("{}")
    val validJsonSource = scala.io.Source.fromString(
        """
          |[
          |  {
          |    "url" : "http://test1",
          |    "title" : "Title",
          |    "content" : "content",
          |    "ancestors" : [
          |      "http://test2",
          |      "http://test3"
          |    ]
          |  }
          | ]
        """.stripMargin)

    val pageListForValidJson = List(
        PageElement(
            "http://test1",
            "Title",
            "content",
            List(
                "http://test2",
                "http://test3"
            )
        )
    )
}
