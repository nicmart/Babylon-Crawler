package babylon.search.config

import java.io.File

import babylon.search.index.IndexInitialiser
import babylon.search.infrastructure.index.{LuceneIndexer, PageToLuceneDocument}
import babylon.search.infrastructure.search.{LuceneSearchService, LuceneToSearchResponse, TemplateBasedSearchToLuceneQuery}
import babylon.search.loader.JsonPageListLoader
import babylon.crawler.output.Output.PageList
import io.circe.Decoder
import io.circe.generic.auto._
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.RAMDirectory

/**
  * Created by Nicolò Martini on 20/04/2017.
  */
object Wiring {

    lazy val cacheFile = new File("cache/pages.json")
    lazy val luceneQueryTemplate = "title:({}) OR fulltitle:({})"

    lazy val pageListLoader = new JsonPageListLoader(pageListDecoder, scala.io.Source.fromFile(cacheFile))
    lazy val pageListDecoder = implicitly[Decoder[PageList]]
    lazy val searchService = new LuceneSearchService(
        searchToLuceneQuery,
        luceneIndexSearcher,
        luceneToSearchResponse
    )
    lazy val indexInitialiser = new IndexInitialiser(pageListLoader, indexer, luceneIndexDirectory)
    lazy val luceneIndexDirectory = new RAMDirectory
    //lazy val luceneAnalyzer = new StandardAnalyzer()
    lazy val luceneAnalyzer = new EnglishAnalyzer()
    lazy val pageToLuceneDocument = PageToLuceneDocument
    lazy val luceneIndexerConfig = new IndexWriterConfig(luceneAnalyzer)
    lazy val luceneIndexReader = DirectoryReader.open(luceneIndexDirectory)
    lazy val luceneIndexSearcher = new IndexSearcher(luceneIndexReader)
    lazy val luceneQueryParser = new QueryParser("title", luceneAnalyzer)
    lazy val searchToLuceneQuery = new TemplateBasedSearchToLuceneQuery(luceneQueryTemplate, luceneQueryParser)
    lazy val luceneToSearchResponse = new LuceneToSearchResponse(luceneIndexSearcher)
    lazy val indexer = new LuceneIndexer(pageToLuceneDocument, luceneIndexerConfig)

}
