package babylon.search.lucene.index

import babylon.common.format.PageElement
import org.scalatest.{Matchers, WordSpec}

class PageToLuceneDocumentSpec extends WordSpec with Matchers {
    import PageToLuceneDocumentSpec._
    "A PageToLuceneDocument" must {
        "not break the title if no separators are provided" in {
            val pageToLuceneDocument = new PageToLuceneDocument(None)
            val doc = pageToLuceneDocument(pageElement)
            doc.get("title_0") shouldBe "Title / Subtitle / Subsubtitle"
            doc.get("title_1") shouldBe null
        }
        "break the title into components if a separator is provided" in {
            val pageToLuceneDocument = new PageToLuceneDocument(Some(" / "))
            val doc = pageToLuceneDocument(pageElement)
            doc.get("title_0") shouldBe "Title"
            doc.get("title_1") shouldBe "Subtitle"
            doc.get("title_2") shouldBe "Subsubtitle"
        }
        "save the entire title into 'fulltitle'" in {
            val pageToLuceneDocument = new PageToLuceneDocument(Some(" / "))
            val doc = pageToLuceneDocument(pageElement)
            doc.get("fulltitle") shouldBe "Title / Subtitle / Subsubtitle"
        }
        "save the page id" in {
            val pageToLuceneDocument = new PageToLuceneDocument(Some(" / "))
            val doc = pageToLuceneDocument(pageElement)
            doc.get("page_id").toInt shouldBe pageElement.hashCode()
        }
    }
}

object PageToLuceneDocumentSpec {
    val pageElement = PageElement(
        "http://test",
        "Title / Subtitle / Subsubtitle",
        "My Content"
    )
}
