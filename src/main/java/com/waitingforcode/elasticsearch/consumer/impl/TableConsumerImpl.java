package com.waitingforcode.elasticsearch.consumer.impl;

import com.waitingforcode.elasticsearch.consumer.TableConsumer;
import com.waitingforcode.rest.converter.SearchHitConverters;
import com.waitingforcode.rest.dto.table.PlaceDto;
import com.waitingforcode.rest.dto.table.TableDto;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TableConsumerImpl implements TableConsumer {

    @Override
    public TableDto getTableForSeason(SearchResponse searchResponse, String season) {
        // it's already ordered by ElasticSearch, so do not make this twice and use LinkedList to store places in correct order
        TableDto table = new TableDto(season, new LinkedList<>());
        Stream.of(searchResponse.getHits().getHits())
                .forEach(searchHit -> table.addPlace(SearchHitConverters.toPlace().convert(searchHit)));
        return table;
    }

    @Override
    public List<PlaceDto> getPlacesByTeamForEachSeason(SearchResponse searchResponse) {
        return Stream.of(searchResponse.getHits().getHits())
                .map(searchHit -> SearchHitConverters.toPlace().convert(searchHit))
                .collect(Collectors.toList());
    }
}
