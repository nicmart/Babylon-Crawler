package babylon.common.format

/**
  * Describes the common page format type
  */
object PageFormat {
    type PageList = List[PageElement]
    val empty: PageList = List()
}

final case class PageElement(
    url: String,
    title: String,
    content: String,
    ancestors: List[String] = List.empty
)
