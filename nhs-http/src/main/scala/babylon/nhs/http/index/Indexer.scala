package babylon.nhs.http.index

import babylon.nhs.output.Output.PageList
import org.apache.lucene.store.Directory

/**
  * Index a PageList inside a luce store Directory
  */
trait Indexer {
    def index(pageList: PageList, directory: Directory)
}
