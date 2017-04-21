package babylon.search.infrastructure.search

import java.util.regex.Matcher

import babylon.search.service.SearchQuery
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil
import org.apache.lucene.search.Query

/**
  * Default implementation of a searchToLuceneQuery.
  */
class TemplateBasedSearchToLuceneQuery(
    queryTemplate: String,
    queryParser: QueryParser
) extends (SearchQuery => Query) {
    def apply(searchQuery: SearchQuery): Query = {
        val queryString = Matcher.quoteReplacement(QueryParserUtil.escape(searchQuery.query))
        val query = queryParser.parse(queryTemplate.replaceAll("\\{\\}", queryString))
        query
    }
}
