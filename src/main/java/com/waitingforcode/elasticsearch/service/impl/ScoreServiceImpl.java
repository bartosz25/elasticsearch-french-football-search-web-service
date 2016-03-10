package com.waitingforcode.elasticsearch.service.impl;

import com.waitingforcode.elasticsearch.config.IndexConfig;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.query.aggs.QueryAggs;
import com.waitingforcode.elasticsearch.query.filters.QueryFilters;
import com.waitingforcode.elasticsearch.query.filters.RangeModes;
import com.waitingforcode.elasticsearch.query.sorting.Sortings;
import com.waitingforcode.elasticsearch.service.ScoreService;
import com.waitingforcode.elasticsearch.util.Pagination;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private IndexConfig index;

    @Override
    public CountableSearchResponse findMatchesByScoredGoalsForTeam(String teamName, int scoredGoals, Pagination pagination) {
        QueryBuilder filterBuilder = QueryBuilders.boolQuery()
                .should(
                    QueryBuilders.boolQuery().must(QueryFilters.hostTeam(teamName))
                        .must(QueryFilters.hostGoals(scoredGoals, RangeModes.GTE))
                )
                .should(
                        QueryBuilders.boolQuery().must(QueryFilters.guestTeam(teamName))
                                .must(QueryFilters.guestGoals(scoredGoals, RangeModes.GTE))
                );

        return new CountableSearchResponse(index.scores()
                .setQuery(QueryBuilders.boolQuery().must(filterBuilder))
                //.setExplain(true)
                .addAggregation(QueryAggs.season())
                .setFrom(pagination.getFrom())
                .setSize(pagination.getPerPage())
                .addSort(Sortings.scoredGoalsByTeamWithFilter(teamName, filterBuilder))
                .addSort(Sortings.defaultSecondSort())
                .get());
    }

    @Override
    public CountableSearchResponse findWonMatchesByScoredGoalsForTeam(String teamName, int scoredGoals, Pagination pagination) {
        QueryBuilder filterBuilder = QueryBuilders.boolQuery()
                .should(
                        QueryBuilders.boolQuery().must(QueryFilters.hostTeam(teamName))
                            .must(QueryFilters.hostGoals(scoredGoals, RangeModes.GTE))
                            .must(QueryFilters.guestGoals(scoredGoals, RangeModes.LT))
                )
                .should(
                        QueryBuilders.boolQuery().must(QueryFilters.guestTeam(teamName))
                            .must(QueryFilters.guestGoals(scoredGoals, RangeModes.GTE))
                            .must(QueryFilters.hostGoals(scoredGoals, RangeModes.LT))
                );

        return new CountableSearchResponse(index.scores()
                .setQuery(QueryBuilders.boolQuery().must(filterBuilder))
                .addAggregation(QueryAggs.season())
                .setFrom(pagination.getFrom())
                .setSize(pagination.getPerPage())
                .addSort(Sortings.scoredGoalsByTeamWithFilter(teamName, filterBuilder))
                .addSort(Sortings.defaultSecondSort())
                .get());
    }

    @Override
    public CountableSearchResponse findAllScoredAndConcededGoalsForTeam(String teamName, Pagination pagination) {
        AggregationBuilder aggregations = QueryAggs.season()
                .subAggregation(QueryAggs.concededHomeGames(teamName))
                .subAggregation(QueryAggs.concededAwayGames(teamName))
                .subAggregation(QueryAggs.scoredHomeGames(teamName))
                .subAggregation(QueryAggs.scoredAwayGames(teamName));

        SortBuilder seasonSortBuilder = SortBuilders.fieldSort("season")
                .order(SortOrder.ASC);

        return new CountableSearchResponse(index.scores()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.guestOrHostTeam(teamName)))
                .addAggregation(aggregations)
                .addSort(seasonSortBuilder)
                .setFrom(pagination.getFrom())
                .setSize(pagination.getPerPage())
                .get());
    }

    @Override
    public SearchResponse findHockeyScoresByRoundsAndSeasons(int minHockeyGoals) {
        AggregationBuilder aggregations = QueryAggs.season()
                    .subAggregation(QueryAggs.round());

        return index.scores()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.allGoals(minHockeyGoals, RangeModes.GTE)))
                .addAggregation(aggregations)
                .setSize(0)  // we are only interested by aggregation results
                .get();
    }

    @Override
    public CountableSearchResponse findTheMostPopularScoresByTeam(String teamName) {
        /**
         * Please note that it may be most performant to store "score" field directly, but for learning purposes
         * where we want to see script query working, we prefer to not do it.
         */
        AggregationBuilder aggregations = QueryAggs.season()
                .subAggregation(QueryAggs.score());

        return new CountableSearchResponse(index.scores()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.guestOrHostTeam(teamName)))
                .addAggregation(aggregations)
                .setSize(0)
                .get());
    }

    @Override
    public CountableSearchResponse findTeamsScoredTheMostGoalsAgainst(String teamName) {
        AggregationBuilder aggregations = QueryAggs.adversary(teamName)
                .subAggregation(QueryAggs.hostTeam(teamName, QueryAggs.guestGoals(), SearchDictionary.AWAY_ADVERSARY_GOALS))
                .subAggregation(QueryAggs.guestTeam(teamName, QueryAggs.hostGoals(), SearchDictionary.HOME_ADVERSARY_GOALS));

        return new CountableSearchResponse(index.scores()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.guestOrHostTeam(teamName)))
                .addAggregation(aggregations)
                .setFrom(0)
                .setSize(0)
                .get());
    }

    @Override
    public SearchResponse findSeasonsForTeamScoredGoalsAtLeastSomeTimes(String teamName, int scoredGoals, int times) {
        QueryBuilder filterBuilder = QueryBuilders.boolQuery()
                .should(
                        QueryBuilders.boolQuery().must(QueryFilters.hostTeam(teamName))
                                .must(QueryFilters.hostGoals(scoredGoals, RangeModes.GTE))
                )
                .should(
                        QueryBuilders.boolQuery().must(QueryFilters.guestTeam(teamName))
                                .must(QueryFilters.guestGoals(scoredGoals, RangeModes.GTE))
                );

        return index.scores()
                .setQuery(QueryBuilders.boolQuery().must(filterBuilder))
                .addAggregation(QueryAggs.season().minDocCount(times))
                .setFrom(0)
                .setSize(0)
                .get();
    }

    @Override
    public SearchResponse findMaxScoredGoalsForTeam(String teamName) {
        AggregationBuilder aggregations = QueryAggs.team(teamName)
                .subAggregation(QueryAggs.hostTeam(teamName, QueryAggs.maxHostGoals(), SearchDictionary.HOME_GOALS))
                .subAggregation(QueryAggs.guestTeam(teamName, QueryAggs.maxGuestGoals(), SearchDictionary.AWAY_GOALS));

        return index.scores()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.guestOrHostTeam(teamName)))
                .addAggregation(aggregations)
                .setSize(0) // size=0 to not show hit details and only return aggregation results
                .get();
    }

    @Override
    public List<SearchResponse> findGamesResultsByTeamAndScoredGoals(String teamName, int maxScoredGoals, ScoreTypes scoreType) {
        List<SearchResponse> responses = new ArrayList<>(maxScoredGoals-1);
        for (int scoredGoals = 0; scoredGoals <= maxScoredGoals; scoredGoals++) {
            QueryBuilder scoreFilter = scoreType.getScoreFilter(teamName, scoredGoals);
            responses.add(getResponseForGamesResultByTeamAndGoals(scoreFilter));
        }
        return responses;
    }

    private SearchResponse getResponseForGamesResultByTeamAndGoals(QueryBuilder filterBuilder) {
        return index.scores()
                .setQuery(QueryBuilders.boolQuery().must(filterBuilder))
                .addAggregation(QueryAggs.season())
                .get();
    }

}