package babylon.search.lucene.index

import babylon.common.format.PageElement
import babylon.search.index.Indexer
import babylon.common.format.PageFormat.PageList
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
