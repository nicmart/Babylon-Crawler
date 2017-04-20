package babylon.nhs.http.infrastructure.search

import babylon.nhs.http.search.SearchQuery
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query

/**
  * Default implementation of a searchToLuceneQuery.
  */
class SearchToLuceneQuery(queryParser: QueryParser) extends (SearchQuery => Query) {
    def apply(searchQuery: SearchQuery): Query = {
        val queryString = searchQuery.query
        queryParser.parse(
            s"title:($queryString)^2 OR subtitle:($queryString) OR content:($queryString)"
        )
    }
}
