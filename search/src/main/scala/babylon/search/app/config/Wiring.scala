package babylon.search.app.config

import java.io.File

import scala.collection.JavaConverters._
import babylon.search.index.IndexInitialiser
import babylon.search.lucene.index.{LuceneIndexer, PageToLuceneDocument, TFOnlySimilarity}
import babylon.search.lucene.search._
import babylon.common.format.PageFormat.PageList
import babylon.common.loader.JsonPageListLoader
import babylon.common.repository.PageElementRepository
import babylon.search.http.RestService
import io.circe.Decoder
import io.circe.generic.auto._
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.RAMDirectory
import org.slf4j.LoggerFactory

/**
  * Created by Nicolò Martini on 20/04/2017.
  */
trait Wiring {
    lazy val cacheFile = new File("cache/pages.json")
    lazy val titleSeparator = " - "
    lazy val luceneQueryTemplate = "title_0:({})^3 title_1:({})^2 fulltitle:({})"
    lazy val stopWords = List(
        "a", "an", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "such",
        "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with",
        "nhs", "choice"
    )

    lazy val httpService = RestService.httpService(searchService)
    lazy val pageListLoader = new JsonPageListLoader(pageListDecoder, scala.io.Source.fromFile(cacheFile))
    lazy val pageElementRepository = PageElementRepository.fromLoader(pageListLoader)
    lazy val pageListDecoder = implicitly[Decoder[PageList]]
    lazy val searchService = new LuceneSearchService(
        searchToLuceneQuery,
        luceneIndexSearcher,
        luceneToSearchResponse
    )
    lazy val indexInitialiser = new IndexInitialiser(pageElementRepository, indexer)
    lazy val pageToLuceneDocument = new PageToLuceneDocument(Some(titleSeparator))

    lazy val luceneIndexDirectory = new RAMDirectory
    lazy val luceneCharArrayStopWords = new CharArraySet(stopWords.asJavaCollection, true)
    lazy val luceneSimilarity = new TFOnlySimilarity
    lazy val luceneAnalyzer = new EnglishAnalyzer(luceneCharArrayStopWords)
    lazy val luceneIndexConfig = new IndexWriterConfig(luceneAnalyzer).setSimilarity(luceneSimilarity)
    lazy val luceneIndexReader = DirectoryReader.open(luceneIndexDirectory)
    lazy val luceneIndexSearcher = {
        val searcher = new IndexSearcher(luceneIndexReader)
        searcher.setSimilarity(luceneSimilarity)
        searcher
    }
    // Query parser is not thread-safe, and we need a new instance every time
    def luceneQueryParser = new QueryParser("title", luceneAnalyzer)
    lazy val searchToLuceneQuery = new LoggingSearchToLuceneQuery(templateSearchToLuceneQuery, logger)
    lazy val templateSearchToLuceneQuery = new TemplateBasedSearchToLuceneQuery(luceneQueryTemplate, luceneQueryParser)
    lazy val luceneDocRepository = LuceneDocRepository(luceneIndexSearcher)
    lazy val luceneToSearchResponse = new LuceneToSearchResponse(luceneDocRepository, pageElementRepository)
    lazy val luceneIndexWriter = new IndexWriter(luceneIndexDirectory, luceneIndexConfig)
    lazy val indexer = new LuceneIndexer(pageToLuceneDocument, luceneIndexWriter)
    lazy val logger = LoggerFactory.getLogger("main")
}

object Wiring extends Wiring
