package babylon.nhs.app

import java.util.concurrent.{ExecutorService, Executors}

import babylon.nhs.service.RestService
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index._
import org.apache.lucene.store.RAMDirectory

import scala.util.Properties.envOrNone
import scalaz.concurrent.Task
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import io.circe.generic.auto._
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, TermQuery}


object HttpApp extends ServerApp {

    val port : Int              = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
    val ip   : String           = "0.0.0.0"
    val pool : ExecutorService  = Executors.newCachedThreadPool()

    val config = DefaultConfig()
    val directory = new RAMDirectory
    val analyzer = new StandardAnalyzer()
    val writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))

    for (page <- config.loader.load().right.get) {
        var doc = new Document

        val titleParts = page.title.split(" - ").toSeq
        val title: String = titleParts(0)
        val subtitle: String = if (titleParts.size == 3) titleParts(1) else ""

        doc.add(new TextField("fulltitle", page.title, Field.Store.YES))
        doc.add(new TextField("title", title, Field.Store.YES))
        doc.add(new TextField("subtitle", subtitle, Field.Store.YES))
        doc.add(new TextField("content", page.content, Field.Store.YES))
        doc.add(new StringField("uri", page.url, Field.Store.YES))

        writer.addDocument(doc)
        println(writer.numDocs + " document(s) indexed.")
    }

    writer.close()

    // Create a searcher and get some results
    val indexReader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(indexReader)
    val parser = new QueryParser("title", analyzer)
    val queryString = "penis"
    val query = parser.parse(s"title:($queryString)^2 OR subtitle:($queryString) OR content:($queryString)")
    val results = searcher.search(query, 50)
    println(results.totalHits + " result(s).")

    // Present the first (and only) hit

    for (scoreDoc <- results.scoreDocs.take(15)) {
        val doc = searcher.doc(scoreDoc.doc)
        println(s"Score: ${scoreDoc.score}")
        println(doc.getField("fulltitle").stringValue)
        println(doc.getField("uri").stringValue)
    }

    override def server(args: List[String]): Task[Server] =
        BlazeBuilder
            .bindHttp(port, ip)
            .mountService(RestService())
            .withServiceExecutor(pool)
            .start
}