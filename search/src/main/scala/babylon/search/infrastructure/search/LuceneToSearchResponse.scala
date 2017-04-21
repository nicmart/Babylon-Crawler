package babylon.search.infrastructure.search

import babylon.search.service.{SearchResponse, SearchResponseItem}
import org.apache.lucene.search.{IndexSearcher, TopDocs}

/**
  * Default conversion of a TopDocs Lucene response to a domain SearchResponse
  */
class LuceneToSearchResponse(searcher: IndexSearcher) extends (TopDocs => SearchResponse) {
    def apply(results: TopDocs): SearchResponse = {
        val documents = results.scoreDocs.map { scoreDoc =>
            searcher.doc(scoreDoc.doc)
        }

        val searchItems = documents.map { doc =>
            SearchResponseItem(
                doc.getField("fulltitle").stringValue(),
                doc.getField("uri").stringValue()
            )
        }

        SearchResponse(searchItems.toList)
    }
}
