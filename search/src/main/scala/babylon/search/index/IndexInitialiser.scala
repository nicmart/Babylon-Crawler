package babylon.search.index

import babylon.crawler.output.Output
import babylon.search.loader.PageListLoader

/**
  * Build the index using an indexer and a pageLoader
  */
class IndexInitialiser(
    pageLoader: PageListLoader,
    indexer: Indexer
) extends (() => Unit) {
    def apply(): Unit = {
        val pages = pageLoader.load().getOrElse(Output.empty)
        indexer.index(pages)
    }
}
