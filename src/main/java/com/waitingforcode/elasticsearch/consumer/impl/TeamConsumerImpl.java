package com.waitingforcode.elasticsearch.consumer.impl;

import com.waitingforcode.elasticsearch.consumer.TeamConsumer;
import com.waitingforcode.elasticsearch.transformer.TeamTransformers;
import com.waitingforcode.rest.dto.team.TeamDto;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TeamConsumerImpl implements TeamConsumer {

    @Override
    public List<TeamDto> getTeamsFromResponse(SearchResponse response) {
        return Stream.of(response.getHits().getHits())
                .map(TeamTransformers.fromSearchHit())
                .collect(Collectors.toList());
    }
}
