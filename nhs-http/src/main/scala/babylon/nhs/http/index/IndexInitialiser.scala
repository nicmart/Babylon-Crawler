package babylon.nhs.http.index

import babylon.nhs.http.loader.PageListLoader
import org.apache.lucene.store.Directory

/**
  * Build the index using an indexer and a pageLoader
  */
class IndexInitialiser(
    pageLoader: PageListLoader,
    indexer: Indexer,
    directory: Directory
) extends (() => Unit) {
    def apply(): Unit = {
        val pages = pageLoader.load().getOrElse(List())
        indexer.index(pages, directory)
    }
}
