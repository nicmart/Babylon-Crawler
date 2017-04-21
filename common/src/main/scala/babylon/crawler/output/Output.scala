package babylon.crawler.output

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
object Output {
    type PageList = List[PageElement]
}

final case class PageElement(url: String, title: String, content: String, ancestors: List[String])
