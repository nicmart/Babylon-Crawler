package babylon.search.lucene.search

import babylon.search.service.SearchQuery
import org.apache.lucene.search.Query
import org.slf4j.Logger

/**
  * Wrap a search query to lucene query transformer logging the trosforming and the transformed query
  */
class LoggingSearchToLuceneQuery(inner: SearchQuery => Query, logger: Logger) extends (SearchQuery => Query){
    def apply(searchQuery: SearchQuery): Query = {
        logger.info(s"Transforming query '${searchQuery.query}'")
        val luceneQuery = inner(searchQuery)
        logger.info(s"Query transformed to '${luceneQuery.toString}'")
        luceneQuery
    }
}
