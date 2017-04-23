package babylon.search.lucene.search

import babylon.search.service.{SearchQuery, SearchResponse, SearchService}
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.{IndexSearcher, Query, TopDocs}
import org.slf4j.Logger

import scala.concurrent.Future

/**
  * Lifts a lucene index searcher to our domain SearchService
  */
class LuceneSearchService(
    searchToLuceneQuery: SearchQuery => Query,
    searcher: IndexSearcher,
    luceneDocsToSearchResponse: (SearchQuery, TopDocs) => SearchResponse
) extends SearchService {
    def search(searchQuery: SearchQuery): SearchResponse = {
        luceneDocsToSearchResponse(
            searchQuery,
            searcher.search(searchToLuceneQuery(searchQuery), searchQuery.limit)
        )
    }
}

/**
  * Index Searcher used for debug. It logs the explanation of the score for each document
  */
class ExplainIndexSearcer(indexReader: IndexReader, logger: Logger) extends IndexSearcher(indexReader) {
    override def search(query: Query, n: Int): TopDocs = {
        val topDocs = super.search(query, n)
        topDocs.scoreDocs.foreach { scoreDoc =>
            logger.info(doc(scoreDoc.doc).getField("fulltitle").toString)
            logger.info(explain(query, scoreDoc.doc).toString)
        }
        topDocs
    }
}
