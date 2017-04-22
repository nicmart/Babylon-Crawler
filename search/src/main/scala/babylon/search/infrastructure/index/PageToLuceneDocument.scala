package babylon.search.infrastructure.index

import babylon.crawler.output.PageElement
import org.apache.lucene.document.{Document, Field, StringField, TextField}

/**
  * Convert a PageElement to a lucene document for indexing
  */
object PageToLuceneDocument extends (PageElement => Document) {
    def apply(page: PageElement): Document = {
        val doc = new Document
        val titleParts = page.title.split(" - ").toSeq
        val title: String = titleParts.head
        val subtitle: String = if (titleParts.size == 3) titleParts(1) else ""
        val fulltitle = titleParts.takeWhile(_.toLowerCase != "nhs choices").mkString(" - ")

        doc.add(new TextField("fulltitle", fulltitle, Field.Store.YES))
        doc.add(new TextField("title", title, Field.Store.YES))
        doc.add(new TextField("subtitle", subtitle, Field.Store.YES))
        doc.add(new TextField("content", page.content, Field.Store.YES))
        doc.add(new StringField("uri", page.url, Field.Store.YES))

        doc
    }
}
