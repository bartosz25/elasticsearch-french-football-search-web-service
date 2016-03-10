package com.waitingforcode.elasticsearch.consumer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreStatsDto;
import com.waitingforcode.rest.dto.score.StatsDto;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
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
public class ScoreConsumerImplTest {

    private static final String FC_SOCHAUX = "FC Sochaux";

    private ScoreConsumerImpl scoreConsumer = new ScoreConsumerImpl();

    @Mock
    private SearchResponse response1, response2;

    @Mock
    private SearchHits hits1, hits2;

    @Mock
    private Aggregations aggs1, aggs2;

    @Before
    public void init() {
        when(response1.getHits()).thenReturn(hits1);
        when(response2.getHits()).thenReturn(hits2);

        when(response1.getAggregations()).thenReturn(aggs1);
    }

    @Test
    public void should_get_correct_2_scores_for_win_type() {
        when(hits1.getTotalHits()).thenReturn(3L);
        when(hits2.getTotalHits()).thenReturn(5L);

        ScoreStatsDto statsDto = scoreConsumer.getStatsForScoreType(Lists.newArrayList(response1, response2), ScoreTypes.WON);

        assertThat(statsDto.getType()).isEqualTo(ScoreTypes.WON);
        assertThat(statsDto.getScores()).hasSize(2);
        assertThat(statsDto.getScores().get(0).getGoals()).isEqualTo(0);
        assertThat(statsDto.getScores().get(0).getGames()).isEqualTo(3);
        assertThat(statsDto.getScores().get(1).getGoals()).isEqualTo(1);
        assertThat(statsDto.getScores().get(1).getGames()).isEqualTo(5);
    }

    @Test
    public void should_correctly_get_max_scored_goals_by_given_team_where_host_goals_are_bigger() {
        initMocksForMaxScoring(3.0d, 1.0d);

        int maxScoredGoals = scoreConsumer.getMaxScoredGoals(response1, FC_SOCHAUX);

        assertThat(maxScoredGoals).isEqualTo(3);
    }

    @Test
    public void should_correctly_get_max_scored_goals_by_given_team_where_guest_goals_are_bigger() {
        initMocksForMaxScoring(3.0d, 5.0d);

        int maxScoredGoals = scoreConsumer.getMaxScoredGoals(response1, FC_SOCHAUX);

        assertThat(maxScoredGoals).isEqualTo(5);
    }

    @Test
    public void should_add_goals_for_two_seasons() {
        Terms.Bucket bucket1 = initScoredBucket("1991/1992", 11d, 12d, 13d, 14d);
        Terms.Bucket bucket2 = initScoredBucket("1992/1993", 21d, 22d, 23d, 24d);
        StringTerms terms1 = mock(StringTerms.class);
        when(aggs1.get("group_by_season")).thenReturn(terms1);
        when(terms1.getBuckets()).thenReturn(Lists.newArrayList(bucket1, bucket2));

        StatsDto statsDto = scoreConsumer.getGoalsBySeason(response1, new StatsDto());

        assertThat(statsDto.getGoals()).hasSize(2);
        assertThat(statsDto.getGoals().get("1991/1992").getScoredHome()).isEqualTo(11);
        assertThat(statsDto.getGoals().get("1991/1992").getConcededHome()).isEqualTo(12);
        assertThat(statsDto.getGoals().get("1991/1992").getScoredAway()).isEqualTo(13);
        assertThat(statsDto.getGoals().get("1991/1992").getConcededAway()).isEqualTo(14);
        assertThat(statsDto.getGoals().get("1992/1993").getScoredHome()).isEqualTo(21);
        assertThat(statsDto.getGoals().get("1992/1993").getConcededHome()).isEqualTo(22);
        assertThat(statsDto.getGoals().get("1992/1993").getScoredAway()).isEqualTo(23);
        assertThat(statsDto.getGoals().get("1992/1993").getConcededAway()).isEqualTo(24);
    }

    @Test
    public void should_correctly_get_hockey_scores() {
        Terms.Bucket bucket1 = initScoresBucket("1991/1992", ImmutableMap.of(1L, 1L, 2L, 2L, 3L, 3L), mock(LongTerms.class));
        Terms.Bucket bucket2 = initScoresBucket("1992/1993", ImmutableMap.of(1L, 4L, 2L, 5L, 3L, 6L), mock(LongTerms.class));
        StringTerms terms1 = mock(StringTerms.class);
        when(aggs1.get("group_by_season")).thenReturn(terms1);
        when(terms1.getBuckets()).thenReturn(Lists.newArrayList(bucket1, bucket2));

        Map<String, List<HockeyScoreDto>> scores = scoreConsumer.getHockeyScores(response1);

        assertThat(scores).hasSize(2);
        assertThat(scores.get("1991/1992")).extracting("round").containsOnly(1L, 2L, 3L);
        assertThat(scores.get("1991/1992")).extracting("games").containsOnly(1L, 2L, 3L);
        assertThat(scores.get("1992/1993")).extracting("round").containsOnly(1L, 2L, 3L);
        assertThat(scores.get("1992/1993")).extracting("games").containsOnly(4L, 5L, 6L);
    }


    @Test
    public void should_correctly_get_most_popular_scores() {
        Terms.Bucket bucket1 = initScoresBucket("1991/1992", ImmutableMap.of(1L, 1L, 2L, 2L, 3L, 3L), mock(StringTerms.class));
        Terms.Bucket bucket2 = initScoresBucket("1992/1993", ImmutableMap.of(1L, 4L, 2L, 5L, 3L, 6L), mock(StringTerms.class));
        StringTerms terms1 = mock(StringTerms.class);
        when(aggs1.get("group_by_season")).thenReturn(terms1);
        when(terms1.getBuckets()).thenReturn(Lists.newArrayList(bucket1, bucket2));

        Map<String, List<BaseScoreDto>> scores = scoreConsumer.getMostPopularScored(response1);

        assertThat(scores).hasSize(2);
        assertThat(scores.get("1991/1992")).extracting("score").containsOnly("1:1", "2:2", "3:3");
        assertThat(scores.get("1991/1992")).extracting("games").containsOnly(1, 2, 3);
        assertThat(scores.get("1992/1993")).extracting("score").containsOnly("1:4", "2:5", "3:6");
        assertThat(scores.get("1992/1993")).extracting("games").containsOnly(4, 5, 6);
    }

    @Test
    public void should_get_correct_places_for_2_seasons() {
        Map<String, Long> seasons = new HashMap<>();
        seasons.put("1991/1992", 15L);
        seasons.put("1992/1993", 18L);
        seasons.put("1993/1994", 27L);
        SearchHit hit9192 = initPlacesHit("1991/1992", 3);
        SearchHit hit9293 = initPlacesHit("1992/1993", 6);
        SearchHit hit9394 = initPlacesHit("1993/1994", 9);
        when(hits1.getHits()).thenReturn(new SearchHit[] {hit9192, hit9293, hit9394});

        List<PlacesByScoredGoalsDto> places = scoreConsumer.getPlacesBySeason(response1, seasons);

        assertThat(places).hasSize(3);
        assertThat(places).extracting("season").containsOnly("1991/1992", "1992/1993", "1993/1994");
        assertThat(places).extracting("place").containsOnly(3, 6, 9);
        assertThat(places).extracting("times").containsOnly(15, 18, 27);
    }

    @Test
    public void should_get_correctly_sorted_the_best_adversaries() {
        Terms.Bucket rcLens = initAdversary("RC Lens", 30d, 40d);
        Terms.Bucket lilleOsc = initAdversary("Lille OSC", 50d, 60d);
        StringTerms terms1 = mock(StringTerms.class);
        when(aggs1.get("group_by_teams")).thenReturn(terms1);
        when(terms1.getBuckets()).thenReturn(Lists.newArrayList(rcLens, lilleOsc));

        List<AdversaryDto> adversaries = scoreConsumer.getBestScorerAdversaries(response1);

        assertThat(adversaries).hasSize(2);
        assertThat(adversaries).extracting("team").containsOnly("RC Lens", "Lille OSC");
        assertThat(adversaries).extracting("goals").containsOnly(110, 70);
        assertThat(adversaries).extracting("scoredAsHost").containsOnly(30, 50);
        assertThat(adversaries).extracting("scoredAsGuest").containsOnly(40, 60);
    }

    public static Terms.Bucket initAdversary(String teamName, double hostGoalsValue, double guestGoalsValue) {
        Terms.Bucket bucket = mock(Terms.Bucket.class);
        when(bucket.getKeyAsString()).thenReturn(teamName);
        Map<String, Aggregation> aggregationMap = new HashMap<>();
        InternalFilter scoresHome = mock(InternalFilter.class);
        InternalSum hostGoals = mock(InternalSum.class);
        when(hostGoals.getValue()).thenReturn(hostGoalsValue);
        when(hostGoals.getName()).thenReturn("goals");
        when(scoresHome.getAggregations()).thenReturn(new InternalAggregations(Lists.<InternalAggregation>newArrayList(hostGoals)));

        InternalFilter scoresAway = mock(InternalFilter.class);
        InternalSum guestGoals = mock(InternalSum.class);
        when(guestGoals.getValue()).thenReturn(guestGoalsValue);
        when(guestGoals.getName()).thenReturn("goals");
        when(scoresAway.getAggregations()).thenReturn(new InternalAggregations(Lists.<InternalAggregation>newArrayList(guestGoals)));


        aggregationMap.put(SearchDictionary.HOME_ADVERSARY_GOALS, scoresHome);
        aggregationMap.put(SearchDictionary.AWAY_ADVERSARY_GOALS, scoresAway);
        Aggregations aggregations = mock(Aggregations.class);

        when(bucket.getAggregations()).thenReturn(aggregations);
        when(aggregations.asMap()).thenReturn(aggregationMap);
        return bucket;
    }

    private SearchHit initPlacesHit(String season, int place) {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = new HashMap<>();
        data.put("season", season);
        data.put("place", place);
        when(hit.sourceAsMap()).thenReturn(data);
        return hit;
    }

    private Terms.Bucket initScoresBucket(String season, Map<Long, Long> roundGamesMap, InternalTerms aggregation) {
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsString()).thenReturn(season);
        Aggregations aggsBucket1 = mock(Aggregations.class);
        when(bucket1.getAggregations()).thenReturn(aggsBucket1);
        List<Terms.Bucket> buckets = new ArrayList<>();
        for (Map.Entry<Long, Long> roundGame : roundGamesMap.entrySet()) {
            Terms.Bucket goalsBucket = mock(Terms.Bucket.class);
            when(goalsBucket.getKeyAsNumber()).thenReturn(roundGame.getKey());
            when(goalsBucket.getDocCount()).thenReturn(roundGame.getValue());
            when(goalsBucket.getKeyAsString()).thenReturn(roundGame.getKey()+":"+roundGame.getValue());
            buckets.add(goalsBucket);
        }
        when(aggregation.getBuckets()).thenReturn(buckets);
        when(aggsBucket1.asList()).thenReturn(Lists.<Aggregation>newArrayList(aggregation));
        return bucket1;
    }

    private Terms.Bucket initScoredBucket(String season, double scoredHome, double concededHome, double scoredAway, double concededAway) {
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsString()).thenReturn(season);
        Aggregations aggsBucket1 = mock(Aggregations.class);

        InternalFilter homeGoals1Sc = mock(InternalFilter.class);
        InternalSum internal1 = mock(InternalSum.class);
        when(internal1.getValue()).thenReturn(scoredHome);
        when(homeGoals1Sc.getName()).thenReturn("scored_home_games");
        when(homeGoals1Sc.getAggregations()).thenReturn(new InternalAggregations(Lists.<InternalAggregation>newArrayList(internal1)));

        InternalFilter homeGoals1Co = mock(InternalFilter.class);
        InternalSum internal2 = mock(InternalSum.class);
        when(internal2.getValue()).thenReturn(concededHome);
        when(homeGoals1Co.getName()).thenReturn("conceded_home_games");
        when(homeGoals1Co.getAggregations()).thenReturn(new InternalAggregations(Lists.<InternalAggregation>newArrayList(internal2)));

        InternalFilter awayGoals1Sc = mock(InternalFilter.class);
        InternalSum internal3 = mock(InternalSum.class);
        when(internal3.getValue()).thenReturn(scoredAway);
        when(awayGoals1Sc.getName()).thenReturn("scored_away_games");
        when(awayGoals1Sc.getAggregations()).thenReturn(new InternalAggregations(Lists.<InternalAggregation>newArrayList(internal3)));

        InternalFilter awayGoals1Co = mock(InternalFilter.class);
        InternalSum internal4 = mock(InternalSum.class);
        when(internal4.getValue()).thenReturn(concededAway);
        when(awayGoals1Co.getName()).thenReturn("conceded_away_games");
        when(awayGoals1Co.getAggregations()).thenReturn(new InternalAggregations(Lists.<InternalAggregation>newArrayList(internal4)));

        when(bucket1.getAggregations()).thenReturn(aggsBucket1);
        when(aggsBucket1.asList()).thenReturn(Lists.<Aggregation>newArrayList(homeGoals1Sc, homeGoals1Co, awayGoals1Sc, awayGoals1Co));
        return bucket1;
    }

    private void initMocksForMaxScoring(double hostGoals, double guestGoals) {
        StringTerms terms1 = mock(StringTerms.class);
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        Aggregations aggsSochaux = mock(Aggregations.class);
        Map<String, Aggregation> aggsMap = new HashMap<>();
        InternalFilter filter1 = mock(InternalFilter.class);
        InternalFilter filter2 = mock(InternalFilter.class);
        InternalMax max1 = mock(InternalMax.class);
        InternalMax max2 = mock(InternalMax.class);
        InternalAggregations iAggs1 = mock(InternalAggregations.class);
        InternalAggregations iAggs2 = mock(InternalAggregations.class);
        when(filter1.getAggregations()).thenReturn(iAggs1);
        when(iAggs1.get("goals")).thenReturn(max1);
        when(filter2.getAggregations()).thenReturn(iAggs2);
        when(iAggs2.get("goals")).thenReturn(max2);
        aggsMap.put("scores_home", filter1);
        aggsMap.put("scores_away", filter2);
        when(max1.getValue()).thenReturn(hostGoals);
        when(max2.getValue()).thenReturn(guestGoals);
        when(aggs1.get("group_by_teams")).thenReturn(terms1);
        when(terms1.getBucketByKey(FC_SOCHAUX)).thenReturn(bucket1);
        when(bucket1.getAggregations()).thenReturn(aggsSochaux);
        when(aggsSochaux.getAsMap()).thenReturn(aggsMap);
    }

}
