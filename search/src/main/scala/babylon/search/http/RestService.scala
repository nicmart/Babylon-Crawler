package babylon.search.http

import babylon.search.service.{SearchQuery, SearchService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

import scala.util.{Failure, Success, Try}

object QueryParam extends QueryParamDecoderMatcher[String]("query")
object LimitParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

object RestService {
    /**
      * Lift a Domain SearchService into an http4s service
      */
    def httpService(searchService: SearchService) = HttpService {

        case GET -> Root / "search" :? QueryParam(query) +& LimitParam(limit) =>
            val tryResults = Try {
                val searchQuery = SearchQuery(query, limit.getOrElse(10))
                val result = searchService.search(searchQuery)
                result.asJson
            }

            tryResults match {
                case Success(results) => Ok(results)
                case Failure(exception) => InternalServerError(exception.getMessage)
            }
        case _ => Ok("hi")
    }
}
