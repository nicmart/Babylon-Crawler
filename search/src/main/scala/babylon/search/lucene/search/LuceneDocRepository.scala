package babylon.search.lucene.search

import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher

/**
  * This is to avoid to be coupled to the heavy Lucene IndexSearcher api just
  * when we need to retrieve Lucene documents by their id
  */
trait LuceneDocRepository {
    def doc(docID: Int): Document
}

object LuceneDocRepository {
    /**
      * Convert a lucene IndexSearcher to our LuceneDocRepository
      */
    def apply(indexSearcher: IndexSearcher): LuceneDocRepository = new LuceneDocRepository {
        def doc(docID: Int): Document = indexSearcher.doc(docID)
    }
}
