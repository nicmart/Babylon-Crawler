# Babylon Crawler
This project is composed by 3 SBT subprojects
 - Common: define common datatype for the scraped pages
 - Crawler: a web crawler implemented using Akka, Circe, and Scala Scraper
 - Search: a search engine and a http endpoint implemented using Apache Lucene, http4s and Circe.
 
## Configuration
Configurations can be found in the objects

    babylon.search.app.config.Wiring
    babylon.crawler.app.config.Wiring
 
## How to run the crawler
You need first to scrape the pages of the target website running
```
sbt "project crawler" run
```
The crawler by default is throttled in its speed to not reach API rate limits
of the upstream service. By default it goes not faster than 5 pages per second.
That value is configurable, together with other parameters in `babylon.crawler.app.config.Wiring`.

At the end of the scraping a json file will be saved in `cache/pages.json`.

## How to run the http service
After the crawling is done, the search engine http service can be started:
```
sbt "project search" run
```
You can visit then query the search endpoint:
```bash
curl 'http://localhost:8080/search?query=what+are+the+symptoms+of+cancer%3F&limit=10'
```
getting the response:
```json
{
  "query": "what are the symptoms of cancer?",
  "results": [
    {
      "title": "Cancer - Signs and symptoms - NHS Choices",
      "uri": "http://www.nhs.uk/Conditions/Cancer/Pages/Symptoms.aspx",
      "ancestors": [
        "http://www.nhs.uk/conditions/Cancer",
        "http://www.nhs.uk/Conditions/Pages/BodyMap.aspx?Index=C",
        "http://www.nhs.uk/Conditions/Pages/hub.aspx"
      ]
    },
    ...
  ]
}
```

### Tests
You can run the tests for all the 3 subprojects:
```
sbt "project common" test
sbt "project crawler" test
sbt "project search" test
```
