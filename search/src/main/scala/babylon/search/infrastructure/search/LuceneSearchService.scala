package babylon.search.infrastructure.search

import babylon.search.service.{SearchQuery, SearchResponse, SearchService}
import org.apache.lucene.search.{IndexSearcher, Query, TopDocs}

import scala.concurrent.Future

/**
  * Implements a search service with a lucene index searcher
  */
class LuceneSearchService(
    searchToLuceneQuery: SearchQuery => Query,
    searcher: IndexSearcher,
    luceneDocsToSearchResponse: TopDocs => SearchResponse
) extends SearchService{
    def search(searchQuery: SearchQuery): SearchResponse = {
        luceneDocsToSearchResponse {
            searcher.search(searchToLuceneQuery(searchQuery), searchQuery.limit)
        }
    }
}
