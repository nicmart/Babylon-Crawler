package babylon.search.loader

import java.io.FileReader

import babylon.crawler.output.Output
import babylon.search.loader.PageListLoader.PageListLoaderFailure
import babylon.crawler.output.Output.PageList
import io.circe.Decoder

import scala.io.Source
import io.circe.parser._

/**
  * Generate a page list out of nothing (side-effects!)
  */
trait PageListLoader {
    def load(): PageListLoader.Result
}

object PageListLoader {
    type Result = Either[PageListLoaderFailure, PageList]
    case class PageListLoaderFailure(exception: Exception) extends Exception
}

/**
  * Loads a json sctring from a scala IO Source and decode it with a CIRCE decoder
  */
class JsonPageListLoader(jsonDecoder: Decoder[PageList], source: Source) extends PageListLoader {
    def load(): PageListLoader.Result = {
        val jsonString = source.getLines().mkString("\n")
        parse(jsonString).fold (
            failure => Left(PageListLoaderFailure(failure)),
            json => jsonDecoder.decodeJson(json).fold(
                failure => Left(PageListLoaderFailure(failure)),
                Right(_)
            )
        )
    }
}
