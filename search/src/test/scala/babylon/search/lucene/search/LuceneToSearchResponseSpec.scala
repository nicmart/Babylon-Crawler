package babylon.search.lucene.search

import babylon.common.format.PageElement
import babylon.common.format.PageFormat.PageList
import babylon.common.repository.PageElementRepository
import babylon.search.service.{SearchQuery, SearchResponse, SearchResponseItem}
import org.apache.lucene.document.{Document, Field, StringField}
import org.apache.lucene.search.{IndexSearcher, ScoreDoc, TopDocs}
import org.scalatest.{Matchers, WordSpec}

class LuceneToSearchResponseSpec extends WordSpec with Matchers {
    import LuceneToSearchResponseSpec._
    "A LuceneToSearchResponse" must {
        "convert search results to search responses" in {
            val luceneToSearchResponse = new LuceneToSearchResponse(indexSearcher, pageElementRepository)
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
            doc.add(new StringField("page_id", "1234", Field.Store.NO))
            doc
        }
    }
    val pageElementRepository = new PageElementRepository {
        def getAll: PageList = ???
        def get(id: Int): Option[PageElement] = id match {
            case 1234 => Some(pageElement)
            case _ => None
        }
    }
    val pageElement = PageElement(
        "uri",
        "title",
        "content"
    )
}
