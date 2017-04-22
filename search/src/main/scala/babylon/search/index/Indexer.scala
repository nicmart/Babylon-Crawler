package babylon.search.index

import babylon.crawler.output.Output.PageList
import org.apache.lucene.store.Directory

/**
  * Index a PageList inside a luce store Directory
  */
trait Indexer {
    def index(pageList: PageList)
}
