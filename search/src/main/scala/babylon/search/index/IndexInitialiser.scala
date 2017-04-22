package babylon.search.index

import babylon.search.loader.PageListLoader
import org.apache.lucene.store.Directory

/**
  * Build the index using an indexer and a pageLoader
  */
class IndexInitialiser(
    pageLoader: PageListLoader,
    indexer: Indexer
) extends (() => Unit) {
    def apply(): Unit = {
        val pages = pageLoader.load().getOrElse(List())
        indexer.index(pages)
    }
}
