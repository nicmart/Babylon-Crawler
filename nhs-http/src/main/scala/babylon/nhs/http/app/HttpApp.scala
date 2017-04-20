package babylon.nhs.http.app

import java.util.concurrent.{ExecutorService, Executors}

import babylon.nhs.http.config.Wiring
import babylon.nhs.http.service.RestService
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index._
import org.apache.lucene.store.RAMDirectory

import scala.util.Properties.envOrNone
import scalaz.concurrent.Task
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import io.circe.generic.auto._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher

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