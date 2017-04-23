package babylon.common.repository

import babylon.common.format.PageFormat.PageList
import babylon.common.format.{PageElement, PageFormat}
import babylon.common.loader.PageListLoader

/**
  * Retrieve all the available pages, and pages by ID
  */
trait PageElementRepository {
    def get(id: Int): Option[PageElement]
    def getAll: PageList
}

object PageElementRepository {
    def fromLoader(pageListLoader: PageListLoader): PageElementRepository =
        new ByListPageElementRepository(pageListLoader.load().getOrElse(PageFormat.empty))
}

class ByListPageElementRepository(pageList: PageList) extends PageElementRepository {
    val pageMap: Map[Int, PageElement] = pageList.foldLeft[Map[Int, PageElement]](Map.empty) {
        case (map, pageElement) => map + (pageElement.hashCode() -> pageElement)
    }
    def getAll: PageList = pageList
    def get(id: Int): Option[PageElement] = pageMap.get(id)
}
