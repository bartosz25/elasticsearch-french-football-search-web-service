package com.waitingforcode.elasticsearch.config;

import com.waitingforcode.elasticsearch.data.IndexTypes;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexConfig {

    @Autowired
    private Client elasticSearchClient;

    public SearchRequestBuilder teams() {
        return buildSearchRequestForType(IndexTypes.TEAMS.getType());
    }

    public SearchRequestBuilder scores() {
        return buildSearchRequestForType(IndexTypes.SCORES.getType());
    }

    public SearchRequestBuilder tables() {
        return buildSearchRequestForType(IndexTypes.TABLES.getType());
    }

    private SearchRequestBuilder buildSearchRequestForType(String type) {
        return elasticSearchClient.prepareSearch(ElasticSearchConfig.ALIAS).setTypes(type);
    }

}
