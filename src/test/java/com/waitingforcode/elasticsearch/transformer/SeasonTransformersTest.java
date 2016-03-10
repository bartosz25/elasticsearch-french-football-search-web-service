package com.waitingforcode.elasticsearch.transformer;

import com.google.common.collect.Lists;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.rest.dto.score.SeasonDto;
import com.waitingforcode.util.assertion.ScoreDtoAssertion;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SeasonTransformersTest {

    @Test
    public void should_correctly_convert_seasons() {
        SearchHit hit1 = searchHit("2001/2002", "US Valenciennes", "Paris SG", 1, 1, 3);
        SearchHit hit2 = searchHit("2002/2003", "FC Nantes", "Paris SG", 10, 3, 3);
        SearchHit hit3 = searchHit("2002/2003", "Lille OSC", "AS Monaco", 15, 5, 2);

        List<SeasonDto> seasons = SeasonTransformers.fromSearchHitsToSeasons().apply(Lists.<SearchHit>newArrayList(hit1, hit2, hit3));

        assertThat(seasons).hasSize(2);
        assertThat(seasons).extracting("season").containsOnly("2001/2002", "2002/2003");
        seasons.forEach((season) -> {
            if (season.getSeason().equals("2001/2002")) {
                assertThat(season.getScores()).hasSize(1);
                ScoreDtoAssertion.assertThat(season.getScores().get(0)).isFullyCorrect("US Valenciennes", "Paris SG", 1, 1, 3);
            } else {
                assertThat(season.getScores()).hasSize(2);
                assertThat(season.getScores()).extracting("hostTeam").extracting("name").containsOnly("FC Nantes", "Lille OSC");
                assertThat(season.getScores()).extracting("guestTeam").extracting("name").containsOnly("Paris SG", "AS Monaco");
                assertThat(season.getScores()).extracting("hostGoals").containsOnly(3, 5);
                assertThat(season.getScores()).extracting(SearchDictionary.GUEST_GOALS).containsOnly(3, 2);
            }
        });
    }

    private SearchHit searchHit(String season, String hostTeam, String guestTeam, int round, int hostGoals, int guestGoals) {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = new HashMap<>();
        data.put("season", season);
        data.put("hostTeam", hostTeam);
        data.put("guestTeam", guestTeam);
        data.put("round", round);
        data.put("hostGoals", hostGoals);
        data.put(SearchDictionary.GUEST_GOALS, guestGoals);
        when(hit.sourceAsMap()).thenReturn(data);
        return hit;
    }

}
