package babylon.search.infrastructure.index

import babylon.crawler.output.PageElement
import org.apache.lucene.document.{Document, Field, StringField, TextField}

/**
  * Convert a PageElement to a lucene document for indexing
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

        titleParts.zipWithIndex foreach { case (part, i) =>
            doc.add(new TextField(s"title_$i", part, Field.Store.YES))
        }
        doc.add(new TextField("fulltitle", page.title, Field.Store.YES))
        doc.add(new TextField("content", page.content, Field.Store.YES))
        doc.add(new StringField("uri", page.url, Field.Store.YES))

        doc
    }
}
