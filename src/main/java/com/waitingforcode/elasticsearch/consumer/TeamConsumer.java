package com.waitingforcode.elasticsearch.consumer;


import com.waitingforcode.rest.dto.team.TeamDto;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;

public interface TeamConsumer {

    List<TeamDto> getTeamsFromResponse(SearchResponse response);

}
