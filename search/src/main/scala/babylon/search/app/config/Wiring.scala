package babylon.search.app.config

import java.io.File

import scala.collection.JavaConverters._
import babylon.search.index.IndexInitialiser
import babylon.search.infrastructure.index.{LuceneIndexer, PageToLuceneDocument, TFOnlySimilarity}
import babylon.search.infrastructure.search._
import babylon.search.loader.JsonPageListLoader
import babylon.crawler.output.Output.PageList
import babylon.search.http.RestService
import io.circe.Decoder
import io.circe.generic.auto._
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, FieldInvertState, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.similarities.{BM25Similarity, ClassicSimilarity}
import org.apache.lucene.store.RAMDirectory
import org.slf4j.LoggerFactory

/**
  * Created by Nicolò Martini on 20/04/2017.
  */
object Wiring {

    lazy val cacheFile = new File("cache/pages.json")
    lazy val luceneQueryTemplate = "title:({})^3 subtitle:({})^2 fulltitle:({})"
    lazy val stopWords = List(
        "a", "an", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "such",
        "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with"
    )

    lazy val httpService = RestService.httpService
    lazy val pageListLoader = new JsonPageListLoader(pageListDecoder, scala.io.Source.fromFile(cacheFile))
    lazy val pageListDecoder = implicitly[Decoder[PageList]]
    lazy val searchService = new LuceneSearchService(
        searchToLuceneQuery,
        luceneIndexSearcher,
        luceneToSearchResponse
    )
    lazy val indexInitialiser = new IndexInitialiser(pageListLoader, indexer, luceneIndexDirectory)

    lazy val luceneIndexDirectory = new RAMDirectory
    lazy val luceneCharArrayStopWords = new CharArraySet(stopWords.asJavaCollection, true)
    lazy val luceneSimilarity = new TFOnlySimilarity
    lazy val luceneAnalyzer = new EnglishAnalyzer()
    lazy val pageToLuceneDocument = PageToLuceneDocument
    lazy val luceneIndexerConfig = new IndexWriterConfig(luceneAnalyzer).setSimilarity(luceneSimilarity)
    lazy val luceneIndexReader = DirectoryReader.open(luceneIndexDirectory)
    lazy val luceneIndexSearcher = {
        val searcher = new IndexSearcher(luceneIndexReader)
        searcher.setSimilarity(luceneSimilarity)
        searcher
    }
    lazy val luceneQueryParser = new QueryParser("title", luceneAnalyzer)
    lazy val searchToLuceneQuery = new LoggingSearchToLuceneQuery(templateSearchToLuceneQuery, logger)
    lazy val templateSearchToLuceneQuery = new TemplateBasedSearchToLuceneQuery(luceneQueryTemplate, luceneQueryParser)
    lazy val luceneToSearchResponse = new LuceneToSearchResponse(luceneIndexSearcher)
    lazy val indexer = new LuceneIndexer(pageToLuceneDocument, luceneIndexerConfig)
    lazy val logger = LoggerFactory.getLogger("main")
}
