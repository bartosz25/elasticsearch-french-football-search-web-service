package com.waitingforcode.elasticsearch.query.aggs;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;

public enum StringTermsAggs {

    GROUPED_BY_SEASON("group_by_season"),
    GROUPED_BY_TEAMS("group_by_teams");

    private String name;

    private StringTermsAggs(String name) {
        this.name = name;
    }

    public StringTerms getTerm(SearchResponse response) {
        return response.getAggregations().get(name);
    }

}
