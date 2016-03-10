package com.waitingforcode.elasticsearch.query.aggs;

import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import com.waitingforcode.elasticsearch.query.filters.QueryFilters;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;

/**
 * Aggregations used by queries.
 */
public final class QueryAggs {

    private static final Script SCORE_SCRIPT =
            new Script("doc['hostGoals'].value.toString() + ':' + doc['guestGoals'].value.toString()");

    private QueryAggs() {
        throw new ConstructorNotInvokableException();
    }

    public static SumBuilder concededHome() {
        return AggregationBuilders.sum("goals_conceded_home").field(SearchDictionary.GUEST_GOALS);
    }

    public static SumBuilder concededAway() {
        return AggregationBuilders.sum("goals_conceded_away").field(SearchDictionary.HOST_GOALS);
    }

    public static SumBuilder scoredHome() {
        return AggregationBuilders.sum("goals_scored_home").field(SearchDictionary.HOST_GOALS);
    }

    public static SumBuilder scoredAway() {
        return AggregationBuilders.sum("goals_scored_away").field(SearchDictionary.GUEST_GOALS);
    }

    public static SumBuilder guestGoals() {
        return AggregationBuilders.sum(SearchDictionary.GOALS).field(SearchDictionary.GUEST_GOALS);
    }

    public static SumBuilder hostGoals() {
        return AggregationBuilders.sum(SearchDictionary.GOALS).field(SearchDictionary.HOST_GOALS);
    }

    public static MaxBuilder maxHostGoals() {
        return AggregationBuilders.max(SearchDictionary.GOALS).field(SearchDictionary.HOST_GOALS);
    }

    public static MaxBuilder maxGuestGoals() {
        return AggregationBuilders.max(SearchDictionary.GOALS).field(SearchDictionary.GUEST_GOALS);
    }

    public static TermsBuilder season() {
        return AggregationBuilders.terms(SearchDictionary.GROUP_BY_SEASON).field(SearchDictionary.SEASON);
    }

    public static TermsBuilder round() {
        return AggregationBuilders.terms(SearchDictionary.GROUP_BY_ROUND).field(SearchDictionary.ROUND);
    }

    public static TermsBuilder score() {
        return AggregationBuilders.terms(SearchDictionary.SCORES).script(SCORE_SCRIPT);
    }

    /**
     * Groups by adversary team to teamName parameter.
     * @param teamName Team against which we generate an adversary. So, if we have a match Ajaccio - PSG and Ajaccio is teamName,
     *                 the adversary will be PSG.
     * @return Adversary terms builder
     */
    public static TermsBuilder adversary(String teamName) {
        return AggregationBuilders.terms(SearchDictionary.GROUP_BY_TEAMS)
                .script(new Script("doc['hostTeam'].value.equals('" + teamName + "') ? doc['guestTeam'].values : doc['hostTeam'].values"));
    }

    /**
     * Groups by team passed in teamName parameter.
     * @param teamName
     * @return Team terms builder.
     */
    public static TermsBuilder team(String teamName) {
        return AggregationBuilders.terms(SearchDictionary.GROUP_BY_TEAMS)
                .script(new Script("doc['hostTeam'].value.equals('"+teamName+"') ? doc['hostTeam'].values : doc['guestTeam'].values"));
    }

    public static AggregationBuilder concededHomeGames(String teamName) {
        return AggregationBuilders.filter("conceded_home_games").filter(QueryFilters.hostTeam(teamName))
                .subAggregation(QueryAggs.concededHome());
    }

    public static AggregationBuilder concededAwayGames(String teamName) {
        return AggregationBuilders.filter("conceded_away_games").filter(QueryFilters.guestTeam(teamName))
                .subAggregation(QueryAggs.concededAway());
    }

    public static AggregationBuilder scoredHomeGames(String teamName) {
        return AggregationBuilders.filter("scored_home_games").filter(QueryFilters.hostTeam(teamName))
                .subAggregation(QueryAggs.scoredHome());
    }

    public static AggregationBuilder scoredAwayGames(String teamName) {
        return AggregationBuilders.filter("scored_away_games").filter(QueryFilters.guestTeam(teamName))
                .subAggregation(QueryAggs.scoredAway());
    }

    public static AggregationBuilder hostTeam(String myTeamName, AbstractAggregationBuilder subAggregation, String filterName) {
        return AggregationBuilders.filter(filterName).filter(QueryFilters.hostTeam(myTeamName))
                .subAggregation(subAggregation);
    }

    public static AggregationBuilder guestTeam(String myTeamName, AbstractAggregationBuilder subAggregation, String filterName) {
        return AggregationBuilders.filter(filterName).filter(QueryFilters.guestTeam(myTeamName))
                .subAggregation(subAggregation);
    }
}
