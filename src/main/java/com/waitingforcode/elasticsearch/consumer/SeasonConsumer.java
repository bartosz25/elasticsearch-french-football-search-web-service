package com.waitingforcode.elasticsearch.consumer;

import com.waitingforcode.rest.dto.score.SeasonDto;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;
import java.util.Map;

public interface SeasonConsumer {

    List<SeasonDto> getSeasonsFromResponse(SearchResponse response);

    Map<String, Long> getSeasonsWithDocOccurrence(SearchResponse response);

}
