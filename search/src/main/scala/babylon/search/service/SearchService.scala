package babylon.search.service

import scala.concurrent.Future

/**
  * SearchService returns a SearchResponse given a SearchQuery
  */
trait SearchService {
    def search(searchQuery: SearchQuery): SearchResponse
}

/**
  * @param query The Search Query entered by the user
  * @param limit Maximum number of results to return
  */
final case class SearchQuery(query: String, limit: Int)

/**
  * A single result
  */
final case class SearchResponseItem(title: String, uri: String)

/**
  * The list of results
  */
final case class SearchResponse(
    query: String,
    results: List[SearchResponseItem]
)

object SearchService {
    /**
      * A service that always returns the same response, regardless of the query
      */
    def constant(searchResponse: SearchResponse) = new SearchService {
        def search(searchQuery: SearchQuery): SearchResponse = searchResponse
    }
}
