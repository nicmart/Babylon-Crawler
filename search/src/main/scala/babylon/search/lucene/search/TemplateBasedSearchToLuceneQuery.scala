package babylon.search.lucene.search

import java.util.regex.Matcher

import babylon.search.service.SearchQuery
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil
import org.apache.lucene.search.Query

/**
  * Default implementation of a searchToLuceneQuery.
  * We pass queryParser by name because it is not thread safe!
  *
  * @param queryTemplate A Template of a LUCENE query. Every occurence of '{}'
  *                      will be replace with the content of the query string
  * @param queryParser The Lucene Query Parser.
  */
class TemplateBasedSearchToLuceneQuery(
    queryTemplate: String,
    queryParser: => QueryParser
) extends (SearchQuery => Query) {
    def apply(searchQuery: SearchQuery): Query = {
        val queryString = Matcher.quoteReplacement(QueryParserUtil.escape(searchQuery.query))
        val query = queryParser.parse(queryTemplate.replaceAll("\\{\\}", queryString))
        query
    }
}
