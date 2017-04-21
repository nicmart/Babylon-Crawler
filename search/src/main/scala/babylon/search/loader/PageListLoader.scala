package babylon.search.loader

import java.io.FileReader

import babylon.search.loader.PageListLoader.PageListLoaderException
import babylon.crawler.output.Output.PageList
import io.circe.Decoder

import scala.io.Source
import io.circe._
import io.circe.parser._

/**
  * Generate a page list our of nothing (side-effects!)
  */
trait PageListLoader {
    def load(): Either[PageListLoaderException, PageList]
}

object PageListLoader {
    case class PageListLoaderException(exception: Exception) extends Exception
}

class JsonPageListLoader(jsonDecoder: Decoder[PageList], source: Source) extends PageListLoader {
    def load(): Either[PageListLoaderException, PageList] = {
        val jsonString = source.getLines().mkString("\n")
        parse(jsonString) match {
            case Left(failure) => Left(PageListLoaderException(failure))
            case Right(json) => Right(jsonDecoder.decodeJson(json).getOrElse(List()))
        }
    }
}
