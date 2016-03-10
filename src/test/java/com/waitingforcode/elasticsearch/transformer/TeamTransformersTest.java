package com.waitingforcode.elasticsearch.transformer;

import com.waitingforcode.rest.dto.team.TeamDto;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TeamTransformersTest {

    @Test
    public void should_correctly_convert_team_from_search_hit() {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = new HashMap<>();
        data.put("name", "US Valenciennes");
        when(hit.sourceAsMap()).thenReturn(data);

        TeamDto team = TeamTransformers.fromSearchHit().apply(hit);

        assertThat(team.getName()).isEqualTo("US Valenciennes");
    }

}
