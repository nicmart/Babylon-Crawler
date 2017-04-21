package babylon.search.app

import java.util.concurrent.{ExecutorService, Executors}

import babylon.search.config.Wiring
import babylon.search.http.RestService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scala.util.Properties.envOrNone
import scalaz.concurrent.Task

object HttpApp extends ServerApp {

    val port : Int              = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
    val ip   : String           = "0.0.0.0"
    val pool : ExecutorService  = Executors.newCachedThreadPool()

    // Initialise the index
    println("Building the page index")
    Wiring.indexInitialiser.apply()

    override def server(args: List[String]): Task[Server] =
        BlazeBuilder
            .bindHttp(port, ip)
            .mountService(RestService())
            .withServiceExecutor(pool)
            .start
}