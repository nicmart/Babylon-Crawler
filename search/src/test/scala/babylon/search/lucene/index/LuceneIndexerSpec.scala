package babylon.search.lucene.index

import java.lang

import babylon.common.format.PageElement
import org.apache.lucene.document.{Document, Field, StringField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig, IndexableField}
import org.apache.lucene.store.RAMDirectory
import org.scalatest.{Matchers, WordSpec}

class LuceneIndexerSpec extends WordSpec with Matchers {

    import LuceneIndexerSpec._

    "A Lucene Indexer" must {
        "convert each page element to a lucene doc" in {
            var output: Document = null
            val indexer = new LuceneIndexer(pagetToLuceneDoc, writer(output = _))
            indexer.index(pageElements)
            output.toString shouldBe pagetToLuceneDoc(pageElements.head).toString
        }

    }
}

object LuceneIndexerSpec {
    def pagetToLuceneDoc(pageElement: PageElement): Document = {
        val doc = new Document
        doc.add(new StringField("title", pageElement.title, Field.Store.YES))
        doc
    }
    def writer(f: Document => Unit) = new IndexWriter(new RAMDirectory(), new IndexWriterConfig()) {
        override def addDocument(doc: lang.Iterable[_ <: IndexableField]): Long = {
            f(doc.asInstanceOf[Document])
            1
        }
    }
    val pageElements = List(
        PageElement(
            "http://test1",
            "My Title",
            "My Content"
        )
    )

}
