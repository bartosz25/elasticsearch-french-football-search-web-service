# elasticsearch-french-football-search-web-service
French football stats search, based on Elasticsearch

Project in Java 8 using Elasticsearch to make some stats aggregations and full text searches on the documents about French football.

It can be ran with simple `mvn clean install` . All integration tests are executed against embedded ES server. 

Disclaimer : 
The project contains some unit tests mocking Elasticsearch objects (Buckets, SearchHits....). The goal was to convince me 
(and maybe you) about advantages of integration tests in this case. The hidden goal was also to give a concrete example to 
learn Scala and build a kind of ES Scala-based DSL for mocked objects (but it's still in progress).
