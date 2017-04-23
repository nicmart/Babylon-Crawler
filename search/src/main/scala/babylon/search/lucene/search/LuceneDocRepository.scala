package babylon.search.lucene.search

import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher

/**
  * An interface to avoid to be coupled to the heavy Lucene IndexSearcher just
  * when we need to retrieve Lucene documents by their id
  */
trait LuceneDocRepository {
    def doc(docID: Int): Document
}

object LuceneDocRepository {
    def apply(indexSearcher: IndexSearcher): LuceneDocRepository = new LuceneDocRepository {
        def doc(docID: Int): Document = indexSearcher.doc(docID)
    }
}
