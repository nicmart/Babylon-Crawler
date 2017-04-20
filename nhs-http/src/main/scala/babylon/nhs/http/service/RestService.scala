package babylon.nhs.http.service

import babylon.nhs.http.app.DefaultConfig
import babylon.nhs.http.config.Wiring
import babylon.nhs.http.search.SearchQuery
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.QueryParamEncoder._

object QueryParam extends QueryParamDecoderMatcher[String]("query")
object LimitParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
object RestService {

    def apply() = HttpService {

        case GET -> Root / "search" :? QueryParam(query) +& LimitParam(limit)=>
            val searchQuery = SearchQuery(query, limit.getOrElse(10))
            val result = Wiring.searchService.search(searchQuery)
            Ok(result.elements.asJson)

    }
}
