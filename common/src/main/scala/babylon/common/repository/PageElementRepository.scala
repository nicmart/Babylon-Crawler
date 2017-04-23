package babylon.common.repository

import babylon.common.format.PageFormat.PageList
import babylon.common.format.{PageElement, PageFormat}
import babylon.common.loader.PageListLoader

/**
  * A PageElement repository.
  *
  * This is used to load the list of all the pages and to
  * get a PageElement by its id (that is its case class hashcode)
  */
trait PageElementRepository {
    def get(id: Int): Option[PageElement]
    def getAll: PageList
}

object PageElementRepository {
    /**
      * A PageElementRepository that uses a page loader for the initial load
      * of the pages. If the PageListLoader fails, an empty PageList is used
      */
    def fromLoader(pageListLoader: PageListLoader): PageElementRepository =
        new ByListPageElementRepository(
            pageListLoader.load().getOrElse(PageFormat.empty)
        )
}

/**
  * An in-memory implementation of PageElementRepository that receives all
  * the pages in advance
  */
class ByListPageElementRepository(pageList: PageList) extends PageElementRepository {
    def getAll: PageList = pageList
    def get(id: Int): Option[PageElement] = pageMap.get(id)

    private val pageMap: Map[Int, PageElement] =
        pageList.foldLeft[Map[Int, PageElement]](Map.empty) {
            case (map, pageElement) => map + (pageElement.hashCode() -> pageElement)
        }
}
