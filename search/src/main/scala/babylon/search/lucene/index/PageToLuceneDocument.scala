package babylon.search.lucene.index

import babylon.common.format.PageElement
import org.apache.lucene.document._

/**
  * Convert a PageElement to a lucene document for indexing
  *
  * @param titleSeparator An optional string separator that will be used to split the title into subparts
  */
class PageToLuceneDocument(titleSeparator: Option[String]) extends (PageElement => Document) {
    def apply(page: PageElement): Document = {
        val doc = new Document
        val titleParts = titleSeparator match {
            case None => Seq(page.title)
            case Some(separator) => page.title.split(separator).toSeq
        }
        val fulltitle = page.title

        // Store all the parts of the title in fields like "title_n"
        titleParts.zipWithIndex foreach { case (part, i) =>
            doc.add(new TextField(s"title_$i", part, Field.Store.NO))
        }

        // Store the full title in a separate field
        doc.add(new TextField("fulltitle", page.title, Field.Store.NO))

        // We store only the page id
        doc.add(new StringField("page_id", page.hashCode().toString, Field.Store.YES))

        doc
    }
}
