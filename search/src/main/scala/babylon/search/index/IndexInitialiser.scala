package babylon.search.index

import babylon.common.format.PageFormat
import babylon.common.repository.PageElementRepository

/**
  * Build the index using an indexer and a pageLoader
  */
class IndexInitialiser(
    pageElementRepository: PageElementRepository,
    indexer: Indexer
) extends (() => Unit) {
    def apply(): Unit = {
        val pages = pageElementRepository.getAll
        indexer.index(pages)
    }
}
