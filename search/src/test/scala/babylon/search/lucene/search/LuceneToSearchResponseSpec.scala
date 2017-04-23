package babylon.search.lucene.search

import babylon.search.service.{SearchQuery, SearchResponse, SearchResponseItem}
import org.apache.lucene.document.{Document, Field, StringField}
import org.apache.lucene.search.{IndexSearcher, ScoreDoc, TopDocs}
import org.scalatest.{Matchers, WordSpec}

class LuceneToSearchResponseSpec extends WordSpec with Matchers {
    import LuceneToSearchResponseSpec._
    "A LuceneToSearchResponse" must {
        "convert search results to search responses" in {
            val luceneToSearchResponse = new LuceneToSearchResponse(indexSearcher)
            val topDocs = new TopDocs(1, Array(new ScoreDoc(1, 1)), 1)
            val searchQuery = SearchQuery("query", 10)
            val response = luceneToSearchResponse(searchQuery, topDocs)
            val expected = SearchResponse("query", List(SearchResponseItem(
                "title",
                "uri"
            )))
            response shouldBe expected
        }
    }
}

object LuceneToSearchResponseSpec {
    val indexSearcher = new LuceneDocRepository {
        def doc(docID: Int): Document = {
            val doc = new Document
            doc.add(new StringField("fulltitle", "title", Field.Store.NO))
            doc.add(new StringField("uri", "uri", Field.Store.NO))
            doc
        }
    }
}
