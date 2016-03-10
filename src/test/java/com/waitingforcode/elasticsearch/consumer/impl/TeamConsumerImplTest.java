package com.waitingforcode.elasticsearch.consumer.impl;


import com.waitingforcode.rest.dto.team.TeamDto;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamConsumerImplTest {

    private TeamConsumerImpl teamConsumer = new TeamConsumerImpl();

    @Mock
    private SearchResponse response;

    @Mock
    private SearchHit hitLens;

    @Mock
    private SearchHit hitParis;

    @Mock
    private SearchHits hits;

    @Before
    public void init() {
        Map<String, Object> mapLens = new HashMap<>();
        mapLens.put("name", "RC Lens");
        Map<String, Object> mapParis = new HashMap<>();
        mapParis.put("name", "Paris SG");
        when(response.getHits()).thenReturn(hits);
        when(hits.getHits()).thenReturn(new SearchHit[] {hitLens, hitParis});
        when(hitLens.sourceAsMap()).thenReturn(mapLens);
        when(hitParis.sourceAsMap()).thenReturn(mapParis);
    }

    @Test
    public void should_correctly_convert_found_team() {
        List<TeamDto> teams = teamConsumer.getTeamsFromResponse(response);

        assertThat(teams).extracting("name").containsOnly("Paris SG", "RC Lens");
    }

}
