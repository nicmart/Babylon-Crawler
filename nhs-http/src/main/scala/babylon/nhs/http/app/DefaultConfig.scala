package babylon.nhs.http.app

import java.io.File

import babylon.nhs.http.config.Config
import babylon.nhs.http.loader.JsonPageListLoader
import babylon.nhs.http.search.{SearchResponse, SearchResponseItem, SearchService}
import babylon.nhs.output.Output.PageList
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
object DefaultConfig {
    def apply()(implicit decoder: Decoder[PageList], resultEncoder: Encoder[SearchResponse]): Config =
        Config(
            loader = new JsonPageListLoader(
                decoder,
                scala.io.Source.fromFile(
                    new File("cache/pages.json")
                )
            ),
            searchService = SearchService.constant(SearchResponse(List(
                SearchResponseItem(
                    "First result",
                    "http://test1"
                ),
                SearchResponseItem(
                    "Second result",
                    "http://test2"
                )
            )))
        )
}
