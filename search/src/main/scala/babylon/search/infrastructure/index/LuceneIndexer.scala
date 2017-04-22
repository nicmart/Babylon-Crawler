package babylon.search.infrastructure.index

import babylon.search.index.Indexer
import babylon.crawler.output.Output.PageList
import babylon.crawler.output.PageElement
import org.apache.lucene.document.Document
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.Directory

/**
  * An indexer implementation that uses a Lucene IndexWriter
  */
class LuceneIndexer(
    pageToDocument: PageElement => Document,
    indexWriter: => IndexWriter
) extends Indexer {
    def index(pageList: PageList): Unit = {
        for (pageElement <- pageList) {
            indexWriter.addDocument(pageToDocument(pageElement))
        }

        indexWriter.close()
    }
}
