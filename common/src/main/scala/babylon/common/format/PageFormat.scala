package babylon.common.format

/**
  * The output of the crawling process will be a JSON encoding of the
  * PageList data type.
  *
  * The search component will then load decode the same JSON back into
  * a PageList instance
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
