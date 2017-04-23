package babylon.search.lucene.search

import babylon.common.repository.PageElementRepository
import babylon.search.service.{SearchQuery, SearchResponse, SearchResponseItem}
import org.apache.lucene.search.TopDocs

/**
  * Default conversion of a TopDocs Lucene response to a domain SearchResponse
  */
class LuceneToSearchResponse(
    searcher: LuceneDocRepository,
    pageElementRepository: PageElementRepository
) extends ((SearchQuery, TopDocs) => SearchResponse) {

    def apply(searchQuery: SearchQuery, results: TopDocs): SearchResponse = {
        val searchResponseItems = for {
            scoreDoc <- results.scoreDocs
            doc = searcher.doc(scoreDoc.doc)
            pageId = doc.getField("page_id").stringValue().toInt
            pageElement <- pageElementRepository.get(pageId.toInt)
        } yield SearchResponseItem(
            pageElement.title,
            pageElement.url,
            pageElement.ancestors
        )

        SearchResponse(searchQuery.query, searchResponseItems.toList)
    }
}
