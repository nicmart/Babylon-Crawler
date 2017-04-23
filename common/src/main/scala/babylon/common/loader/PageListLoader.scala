package babylon.common.loader

import babylon.common.format.PageFormat.PageList
import babylon.common.loader.PageListLoader.PageListLoaderFailure
import io.circe.Decoder
import scala.io.Source
import io.circe.parser._

/**
  * Generate a page list out of nothing (side-effects!)
  * The load of the page list can fail for a lot of reasons,
  * so the return type is an Either[PageListLoaderFailure, PageList]
  */
trait PageListLoader {
    def load(): PageListLoader.Result
}

object PageListLoader {
    type Result = Either[PageListLoaderFailure, PageList]
    case class PageListLoaderFailure(exception: Exception) extends Exception
}

/**
  * This is an implementation of a pageListLoader that uses a CIRCE Json decoder,
  * and a scala Source
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
