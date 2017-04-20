package babylon.nhs.http.infrastructure.index

import babylon.nhs.http.index.Indexer
import babylon.nhs.output.Output.PageList
import babylon.nhs.output.PageElement
import org.apache.lucene.document.Document
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.Directory

/**
  * An indexer implementation that uses a Lucene IndexWriter
  */
class LuceneIndexer(
    pageToDocument: PageElement => Document,
    indexWriterConfig: IndexWriterConfig
) extends Indexer {
    def index(pageList: PageList, directory: Directory): Unit = {
        val writer = new IndexWriter(directory, indexWriterConfig)

        for (pageElement <- pageList) {
            writer.addDocument(pageToDocument(pageElement))
        }

        writer.close()
    }
}
