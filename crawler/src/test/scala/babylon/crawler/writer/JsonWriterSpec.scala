package babylon.crawler.writer

import io.circe.{Encoder, Json}
import io.circe.parser._
import org.scalatest.{Matchers, WordSpec}


class JsonWriterSpec extends WordSpec with Matchers {

    import JsonWriterSpec._

    "A Json Writer" must {
        "convert an object to a JSON string" in {

            var output = ""
            val stringWriter = Writer[String](output = _)
            val jsonWriter = new JsonWriter[String](encoder, stringWriter)

            val input = "world"
            val expected = parse {
                s"""
                  {"hello": "world"}
                """
            }.right.get.toString()

            jsonWriter.write(input)
            output shouldBe expected
        }
    }
}

object JsonWriterSpec {
    val encoder = new Encoder[String] {
        def apply(a: String): Json = parse {
            s"""
                      {"hello": "$a"}
                    """
        }.right.get
    }
}
