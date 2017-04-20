package babylon.nhs.http.config

import babylon.nhs.http.loader.PageListLoader
import babylon.nhs.http.search.SearchService

case class Config(
    loader: PageListLoader,
    searchService: SearchService
)
