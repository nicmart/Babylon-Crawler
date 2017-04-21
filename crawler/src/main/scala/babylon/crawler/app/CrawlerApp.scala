package babylon.crawler.app

import babylon.crawler.app.config.Wiring

/**
  * Created by nic on 13/04/2017.
  */
object CrawlerApp extends App {

    import Wiring._
    supervisor ! initialMessage

}
