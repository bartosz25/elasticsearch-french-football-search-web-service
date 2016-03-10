package com.waitingforcode.elasticsearch.query;

import com.google.common.base.MoreObjects;
import org.elasticsearch.action.search.SearchResponse;

public class CountableSearchResponse {

    private final long allElements;

    private final SearchResponse dataResponse;

    public CountableSearchResponse(SearchResponse dataResponse) {
        this.dataResponse = dataResponse;
        this.allElements = dataResponse.getHits().getTotalHits();
    }

    public long getAllElements() {
        return allElements;
    }

    public SearchResponse getDataResponse() {
        return dataResponse;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("allElements", allElements).add("dataResponse", dataResponse)
                .toString();
    }

}
