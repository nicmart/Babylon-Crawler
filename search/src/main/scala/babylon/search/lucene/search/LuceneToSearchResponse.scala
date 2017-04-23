package babylon.search.lucene.search

import babylon.search.service.{SearchQuery, SearchResponse, SearchResponseItem}
import org.apache.lucene.search.{IndexSearcher, TopDocs}

/**
  * Default conversion of a TopDocs Lucene response to a domain SearchResponse
  */
class LuceneToSearchResponse(searcher: LuceneDocRepository)
    extends ((SearchQuery, TopDocs) => SearchResponse) {

    def apply(searchQuery: SearchQuery, results: TopDocs): SearchResponse = {
        val documents = results.scoreDocs.map { scoreDoc =>
            searcher.doc(scoreDoc.doc)
        }
        val searchItems = documents.map { doc =>
            SearchResponseItem(
                doc.getField("fulltitle").stringValue(),
                doc.getField("uri").stringValue()
            )
        }
        SearchResponse(searchQuery.query, searchItems.toList)
    }
}
