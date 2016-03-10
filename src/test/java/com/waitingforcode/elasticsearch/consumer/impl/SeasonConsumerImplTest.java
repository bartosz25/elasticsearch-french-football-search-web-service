package com.waitingforcode.elasticsearch.consumer.impl;

import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.rest.dto.score.ScoreDto;
import com.waitingforcode.rest.dto.score.SeasonDto;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SeasonConsumerImplTest {

    private static final String OM = "Olympique de Marseille";
    private static final String OL = "Olympique Lyonnais";
    private static final String OGCN = "OGC Nice";
    private static final String RCL = "RC Lens";

    private SeasonConsumerImpl seasonConsumer = new SeasonConsumerImpl();

    @Mock
    private SearchResponse response;

    @Mock
    private Aggregations aggregations;

    @Mock
    private StringTerms terms1;

    @Mock
    private SearchHits searchHits;

    @Mock
    private SearchHit match1, match2, match3, match4;

    private String score1, score2, score3, score4;

    @Before
    public void init() {
        when(response.getAggregations()).thenReturn(aggregations);
        when(aggregations.get("group_by_season")).thenReturn(terms1);
        when(response.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[] {match1, match2, match3, match4});
        when(match1.sourceAsMap()).thenReturn(matchData(OGCN, OM, 3, 1, 1, "2001/2002"));
        score1 = OGCN+"-"+OM+" 3:1 - 1";
        when(match2.sourceAsMap()).thenReturn(matchData(OL, RCL, 1, 1, 1, "2001/2002"));
        score2 = OL+"-"+RCL+" 1:1 - 1";
        when(match3.sourceAsMap()).thenReturn(matchData(RCL, OM, 2, 2, 2, "2002/2003"));
        score3 = RCL+"-"+OM+" 2:2 - 2";
        when(match4.sourceAsMap()).thenReturn(matchData(OL, OGCN, 4, 0, 2, "2002/2003"));
        score4 = OL+"-"+OGCN+" 4:0 - 2";
    }

    @Test
    public void should_get_two_seasons_from_single_response() {
        List<Terms.Bucket> buckets = getBuckets();
        when(terms1.getBuckets()).thenReturn(buckets);

        List<SeasonDto> seasons = seasonConsumer.getSeasonsFromResponse(response);

        assertThat(seasons).hasSize(2);
        assertThat(seasons).extracting("scores").hasSize(2);
        List<String> gotScores = new ArrayList<>();
        seasons.forEach((SeasonDto season) -> {
            season.getScores().forEach((ScoreDto score) -> {
                gotScores.add(toTested(score));
            });
        });
        assertThat(gotScores).containsOnly(score1, score2, score3, score4);
    }

    @Test
    public void should_get_two_seasons_with_correct_doc_count() {
        List<Terms.Bucket> buckets = getBuckets();
        when(terms1.getBuckets()).thenReturn(buckets);

        Map<String, Long> seasons = seasonConsumer.getSeasonsWithDocOccurrence(response);

        assertThat(seasons).hasSize(2);
        assertThat(seasons.get("2001/2002")).isEqualTo(38L);
        assertThat(seasons.get("2003/2004")).isEqualTo(34L);
    }

    private String toTested(ScoreDto scoreDto) {
        return new StringBuilder(scoreDto.getHostTeam().getName()).append("-").append(scoreDto.getGuestTeam().getName())
                .append(" ").append(scoreDto.getHostGoals()).append(":").append(scoreDto.getGuestGoals())
                .append(" - ").append(scoreDto.getRound()).toString();
    }

    private List<Terms.Bucket> getBuckets() {
        Terms.Bucket mock0102 = mock(Terms.Bucket.class);
        when(mock0102.getKeyAsString()).thenReturn("2001/2002");
        when(mock0102.getDocCount()).thenReturn(38L);
        Terms.Bucket mock0203 = mock(Terms.Bucket.class);
        when(mock0203.getKeyAsString()).thenReturn("2003/2004");
        when(mock0203.getDocCount()).thenReturn(34L);
        List<Terms.Bucket> buckets = new ArrayList<>();
        buckets.add(mock0102);
        buckets.add(mock0203);
        return buckets;
    }

    private Map<String, Object> matchData(String host, String guest, int hostGoals, int guestGoals, int round, String season) {
        Map<String, Object> map = new HashMap<>();
        map.put("hostTeam", host);
        map.put("guestTeam", guest);
        map.put("round", round);
        map.put("hostGoals", hostGoals);
        map.put(SearchDictionary.GUEST_GOALS, guestGoals);
        map.put("season", season);
        return map;
    }
}
