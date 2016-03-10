package com.waitingforcode.elasticsearch.consumer;

import com.waitingforcode.rest.dto.table.PlaceDto;
import com.waitingforcode.rest.dto.table.TableDto;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;

public interface TableConsumer {

    TableDto getTableForSeason(SearchResponse searchResponse, String season);

    List<PlaceDto> getPlacesByTeamForEachSeason(SearchResponse searchResponse);

}
