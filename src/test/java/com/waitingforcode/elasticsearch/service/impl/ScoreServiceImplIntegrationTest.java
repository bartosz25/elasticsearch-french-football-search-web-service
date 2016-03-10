package com.waitingforcode.elasticsearch.service.impl;

import com.google.common.collect.Lists;
import com.waitingforcode.configuration.IntegrationTestConfiguration;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.query.aggs.StringTermsAggs;
import com.waitingforcode.elasticsearch.service.ScoreService;
import com.waitingforcode.elasticsearch.util.InternalSumHelper;
import com.waitingforcode.elasticsearch.util.Pagination;
import com.waitingforcode.util.Indexers;
import com.waitingforcode.util.ScoreInput;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@ActiveProfiles( profiles = {"test"})
public class ScoreServiceImplIntegrationTest {

    private static final String PARIS_SG = "Paris-SG";
    private static final String SC_BASTIA = "SC Bastia";
    private static final String SEASON_0102 = "2001/2002";

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private Indexers indexers;

    @After
    public void reset() {
        indexers.cleanAll("scores");
    }

    @Test
    public void should_find_paris_sg_scored_at_least_3_goals_in_2_of_4_indexed_matches() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(0, 3).build();
        ScoreInput score3 = builder.withTeams("AJ Auxerre", PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(2, 2).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4));

        CountableSearchResponse response = scoreService.findMatchesByScoredGoalsForTeam(PARIS_SG.toLowerCase(), 3, new Pagination());

        assertThat(response.getDataResponse().getHits()).hasSize(2);
        List<String> scoreLines = Stream.of(response.getDataResponse().getHits().getHits()).map(new ToScoreLine()).collect(Collectors.toList());
        assertThat(scoreLines).containsOnly(toScoreCompare(score1), toScoreCompare(score2));
        assertThat(response.getAllElements()).isEqualTo(2);
    }

    @Test
    public void should_find_paris_sg_scored_at_least_3_goals_in_2_of_4_indexed_matches_and_paginate() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(0, 3).build();
        ScoreInput score3 = builder.withTeams("AJ Auxerre", PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(2, 2).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4));
        Pagination pagination = new Pagination();
        pagination.setPerPage(1);

        CountableSearchResponse response = scoreService.findMatchesByScoredGoalsForTeam(PARIS_SG.toLowerCase(), 3, pagination);

        assertThat(response.getDataResponse().getHits()).hasSize(1);
        List<String> scoreLines = Stream.of(response.getDataResponse().getHits().getHits()).map(new ToScoreLine()).collect(Collectors.toList());
        assertThat(scoreLines).containsOnly(toScoreCompare(score2));
        assertThat(response.getAllElements()).isEqualTo(2);
    }

    @Test
    public void should_not_find_scored_goals() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams("US Valenciennes", "Lille OSC").withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", "RC Lens").withGoals(0, 3).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2));

        CountableSearchResponse response = scoreService.findMatchesByScoredGoalsForTeam(PARIS_SG.toLowerCase(), 1, new Pagination());

        assertThat(response.getDataResponse().getHits()).isEmpty();
        assertThat(response.getAllElements()).isEqualTo(0);
    }

    @Test
    public void should_find_hockey_results() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(0, 3).build();
        ScoreInput score3 = builder.withTeams("AJ Auxerre", PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(2, 2).build();
        ScoreInput score5 = builder.forSeason("2004/2005").withTeams(PARIS_SG, "AJ Auxerre").withGoals(1, 2).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5));

        SearchResponse response = scoreService.findHockeyScoresByRoundsAndSeasons(4);

        assertThat(response.getAggregations().asMap()).containsKey("group_by_season");
        StringTerms aggregation = (StringTerms) response.getAggregations().asMap().get("group_by_season");
        assertThat(aggregation.getBuckets()).hasSize(1);
        assertThat(aggregation.getBucketByKey(SEASON_0102).getDocCount()).isEqualTo(3L);
    }

    @Test
    public void should_not_find_hockey_result() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams("US Valenciennes", "Lille OSC").withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", "RC Lens").withGoals(0, 3).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2));

        SearchResponse response = scoreService.findHockeyScoresByRoundsAndSeasons(6);

        assertThat(response.getAggregations().asMap()).containsKey("group_by_season");
        StringTerms aggregation = (StringTerms) response.getAggregations().asMap().get("group_by_season");
        assertThat(aggregation.getBuckets()).isEmpty();
    }

    @Test
    public void should_find_one_the_most_popular_score() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(0, 5).build();
        ScoreInput score3 = builder.withTeams("AJ Auxerre", PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(1, 2).build();
        ScoreInput score5 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(1, 2).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5));

        CountableSearchResponse countableSearchResponse = scoreService.findTheMostPopularScoresByTeam(PARIS_SG.toLowerCase());

        SearchResponse response = countableSearchResponse.getDataResponse();
        StringTerms term = (StringTerms) response.getAggregations().asMap().get("group_by_season");
        StringTerms scoreTerms = (StringTerms) term.getBuckets().get(0).getAggregations().asMap().get("scores");
        assertThat(scoreTerms.getBuckets()).hasSize(4);
        assertThat(scoreTerms.getBucketByKey("1:2").getDocCount()).isEqualTo(2L);
        assertThat(scoreTerms.getBucketByKey("5:0").getDocCount()).isEqualTo(1L);
        assertThat(scoreTerms.getBucketByKey("0:5").getDocCount()).isEqualTo(1L);
        assertThat(scoreTerms.getBucketByKey("3:1").getDocCount()).isEqualTo(1L);
    }

    @Test
    public void should_find_all_teams_as_the_best_team_scoring_against_paris_sg() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 5);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(3, 5).build();
        ScoreInput score3 = builder.withTeams("AS Monaco", PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams("AJ Auxerre", PARIS_SG).withGoals(1, 1).build();
        ScoreInput score5 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(1, 1).build();
        ScoreInput score6 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(1, 3).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6));

        CountableSearchResponse response = scoreService.findTeamsScoredTheMostGoalsAgainst(PARIS_SG.toLowerCase());

        List<Terms.Bucket> buckets = ((StringTerms)response.getDataResponse().getAggregations().get("group_by_teams")).getBuckets();
        List<String> teams = new ArrayList<>();
        List<Double> scoresHome = new ArrayList<>();
        List<Double> scoresAway = new ArrayList<>();
        buckets.forEach((Terms.Bucket bucket) -> {
            teams.add(bucket.getKeyAsString());
            Aggregations aggs = bucket.getAggregations();
            InternalFilter filterHost = aggs.get(SearchDictionary.HOME_ADVERSARY_GOALS);
            InternalSum hostGoals = filterHost.getAggregations().get("goals");
            scoresHome.add(hostGoals.value());
            InternalFilter filterGuest = aggs.get(SearchDictionary.AWAY_ADVERSARY_GOALS);
            InternalSum guestGoals = filterGuest.getAggregations().get("goals");
            scoresAway.add(guestGoals.value());
        });
        // result is not expected to be sorted
        assertThat(teams).contains("AJ Auxerre".toLowerCase(), "Lille OSC".toLowerCase(), "RC Lens".toLowerCase(), "AS Monaco".toLowerCase());
        assertThat(scoresHome).contains(1.0d, 3.0d, 0.0d); // 0 because RC Lens scored only in away game
        assertThat(scoresAway).contains(1.0d, 5.0d, 3.0d, 0.0d); // 0 because AS Monaco scored only in home game
    }

    @Test
    public void should_not_find_sc_bastia_scoring_2_goals_at_least_one_time_in_season_by_using_synonym_sporting_club() {
        // analyzers doesn't work on filter queries
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(SC_BASTIA, "Lille OSC").withGoals(2, 5);
        ScoreInput score1 = builder.build();
        indexers.scoresFromObject(Lists.newArrayList(score1));
        String scbSynonym = "sporting club bastia";

        SearchResponse response = scoreService.findSeasonsForTeamScoredGoalsAtLeastSomeTimes(scbSynonym, 2, 1);

        List<Terms.Bucket> buckets = StringTermsAggs.GROUPED_BY_SEASON.getTerm(response).getBuckets();
        assertThat(buckets).isEmpty();
    }

    @Test
    public void should_find_paris_sg_scoring_2_goals_at_least_one_time_in_2_of_3_indexed_seasons() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 5);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.forSeason("2003/2004").withTeams("Lille OSC", PARIS_SG).withGoals(3, 5).build();
        ScoreInput score3 = builder.forSeason("1990/1991").withTeams("AS Monaco", PARIS_SG).withGoals(3, 1).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3));

        SearchResponse response = scoreService.findSeasonsForTeamScoredGoalsAtLeastSomeTimes(PARIS_SG.toLowerCase(), 2, 1);

        List<String> seasons = new ArrayList<>();
        List<Terms.Bucket> buckets = StringTermsAggs.GROUPED_BY_SEASON.getTerm(response).getBuckets();
        buckets.forEach((Terms.Bucket bucket) -> {
            seasons.add(bucket.getKeyAsString());
            assertThat(bucket.getDocCount()).isEqualTo(1);
        });
        assertThat(seasons).containsOnly(SEASON_0102, "2003/2004");
    }


    @Test
    public void should_not_find_paris_sg_scoring_2_goals_at_least_2_times_in_3_indexed_seasons() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 5);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.forSeason("2003/2004").withTeams("Lille OSC", PARIS_SG).withGoals(3, 5).build();
        ScoreInput score3 = builder.forSeason("1990/1991").withTeams("AS Monaco", PARIS_SG).withGoals(3, 1).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3));

        SearchResponse response = scoreService.findSeasonsForTeamScoredGoalsAtLeastSomeTimes(PARIS_SG.toLowerCase(), 2, 2);

        List<Terms.Bucket> buckets = StringTermsAggs.GROUPED_BY_SEASON.getTerm(response).getBuckets();
        assertThat(buckets).isEmpty();
    }

    @Test
    public void should_find_6_as_max_scored_goals_by_paris_sg() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 5);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.forSeason("2003/2004").withTeams("Lille OSC", PARIS_SG).withGoals(3, 6).build();
        ScoreInput score3 = builder.forSeason("1990/1991").withTeams("AS Monaco", PARIS_SG).withGoals(3, 4).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3));

        SearchResponse response = scoreService.findMaxScoredGoalsForTeam(PARIS_SG.toLowerCase());

        StringTerms groupGoals = response.getAggregations().get("group_by_teams");
        Map<String, Aggregation> aggs = groupGoals.getBucketByKey(PARIS_SG.toLowerCase()).getAggregations().getAsMap();
        InternalMax hostGoals = ((InternalFilter) aggs.get("scores_home")).getAggregations().get("goals");
        InternalMax guestGoals = ((InternalFilter) aggs.get("scores_away")).getAggregations().get("goals");
        assertThat(hostGoals.value()).isEqualTo(2.0d);
        assertThat(guestGoals.value()).isEqualTo(6.0d);
    }

    @Test
    public void should_find_won_games_when_scoring_1_and_2_goals() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 1);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(1, 1).build();
        ScoreInput score3 = builder.withTeams(PARIS_SG, "AS Monaco").withGoals(1, 0).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(1, 0).build();
        ScoreInput score5 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(3, 4).build();
        ScoreInput score6 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(2, 0).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score6));

        List<SearchResponse> responses = scoreService.findGamesResultsByTeamAndScoredGoals(PARIS_SG.toLowerCase(), 2, ScoreTypes.WON);

        List<Long> expected = new ArrayList<>();
        responses.forEach((SearchResponse response) -> {
            expected.add(response.getHits().getTotalHits());
        });
        // two times 1:0, two times: 2:0 and 2:1
        assertThat(expected).contains(0L, 2L, 2L);
    }

    @Test
    public void should_find_draw_games_when_scoring_1_and_2_goals() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 2);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(1, 1).build();
        ScoreInput score3 = builder.withTeams(PARIS_SG, "AS Monaco").withGoals(1, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(1, 1).build();
        ScoreInput score5 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(3, 4).build();
        ScoreInput score6 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(2, 0).build();
        ScoreInput score7 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(0, 0).build();
        ScoreInput score8 = builder.withTeams("AJ Auxerre", "RC Lens").withGoals(0, 0).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score6, score7, score8));

        List<SearchResponse> responses = scoreService.findGamesResultsByTeamAndScoredGoals(PARIS_SG.toLowerCase(), 2, ScoreTypes.DRAW);

        List<Long> expected = new ArrayList<>();
        responses.forEach((SearchResponse response) -> {
            expected.add(response.getHits().getTotalHits());
        });
        // 3 times 1:1, 1 times: 2:2 and 0:0
        assertThat(expected).contains(1L, 3L, 1L);
    }

    @Test
    public void should_find_lost_games_when_scoring_1_and_2_goals() {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(4, 1).build();
        ScoreInput score3 = builder.withTeams(PARIS_SG, "AS Monaco").withGoals(2, 3).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(0, 1).build();
        ScoreInput score5 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(0, 4).build();
        ScoreInput score6 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(2, 0).build();
        ScoreInput score7 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(0, 0).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score6, score7));

        List<SearchResponse> responses = scoreService.findGamesResultsByTeamAndScoredGoals(PARIS_SG.toLowerCase(), 2, ScoreTypes.LOST);

        List<Long> expected = new ArrayList<>();
        responses.forEach((SearchResponse response) -> {
            expected.add(response.getHits().getTotalHits());
        });
        // 0 goals -> 0:1, 0:4; 1 goals -> 4:1, 2 goals -> 2:3
        assertThat(expected).contains(2L, 1L, 1L);
    }

    @Test
    public void should_correctly_sum_scored_and_conceded_goals_for_paris_sg() {
        String seasons_8081 = "1980/1981";
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, "Lille OSC").withGoals(2, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams("Lille OSC", PARIS_SG).withGoals(4, 1).build();
        ScoreInput score3 = builder.withTeams(PARIS_SG, "AS Monaco").withGoals(2, 3).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, "AJ Auxerre").withGoals(0, 1).build();
        ScoreInput score5 = builder.forSeason(seasons_8081).withTeams("AJ Auxerre", PARIS_SG).withGoals(0, 4).build();
        ScoreInput score6 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(2, 0).build();
        ScoreInput score7 = builder.withTeams(PARIS_SG, "RC Lens").withGoals(4, 5).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score7));

        CountableSearchResponse response = scoreService.findAllScoredAndConcededGoalsForTeam(PARIS_SG.toLowerCase(), new Pagination());

        Map<String, Map<String, Integer>> goals = new HashMap<>();
        ((StringTerms)response.getDataResponse().getAggregations().get("group_by_season")).getBuckets().forEach((Terms.Bucket bucket) -> {
            Map<String, Integer> goalsStats = bucket.<Aggregation>getAggregations().asList().stream()
                    .collect(Collectors.toMap(
                            aggregation -> aggregation.getName(),
                            aggregation -> InternalSumHelper
                                    .getInt((InternalSum) ((InternalFilter) aggregation).getAggregations().asList().get(0))
                    ));
            goals.put(bucket.getKeyAsString(), goalsStats);
        });
        assertThat(goals.get(SEASON_0102).get("conceded_away_games")).isEqualTo(4);
        assertThat(goals.get(SEASON_0102).get("conceded_home_games")).isEqualTo(4);
        assertThat(goals.get(SEASON_0102).get("scored_away_games")).isEqualTo(1);
        assertThat(goals.get(SEASON_0102).get("scored_home_games")).isEqualTo(4);
        assertThat(goals.get(seasons_8081).get("conceded_away_games")).isEqualTo(0);
        assertThat(goals.get(seasons_8081).get("conceded_home_games")).isEqualTo(5);
        assertThat(goals.get(seasons_8081).get("scored_away_games")).isEqualTo(4);
        assertThat(goals.get(seasons_8081).get("scored_home_games")).isEqualTo(6);
        assertThat(response.getAllElements()).isEqualTo(7);
    }


    private String toScoreCompare(ScoreInput score) {
        return new StringBuilder(score.getSeason()).append(",").append(score.getRound())
                .append("/").append(score.getHostTeam()).append(" - ").append(score.getGuestTeam())
                .append("/").append(score.getHostGoals()).append(":").append(score.getGuestGoals())
                .append("/").append(score.getAllGoals())
                .toString();
    }

    private static class ToScoreLine implements Function<SearchHit, String> {

        @Override
        public String apply(SearchHit searchHit) {
            Map<String, Object> data = searchHit.sourceAsMap();

            return new StringBuilder(""+data.get("season")).append(",").append(data.get("round"))
                    .append("/").append(data.get("hostTeam")).append(" - ").append(data.get("guestTeam"))
                    .append("/").append(data.get("hostGoals")).append(":").append(data.get(SearchDictionary.GUEST_GOALS))
                    .append("/").append(data.get("allGoals"))
                    .toString();
        }
    }

}
