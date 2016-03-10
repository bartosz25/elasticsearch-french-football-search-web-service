package com.waitingforcode.elasticsearch.query.filters;

import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;

import java.util.Collection;

/**
 * Groups all filters used by queries.
 */
public final class QueryFilters {

    private QueryFilters() {
        throw new ConstructorNotInvokableException();
    }

    public static TermQueryBuilder team(String teamName) {
        return QueryBuilders.termQuery("team", teamName);
    }

    public static TermQueryBuilder season(String season) {
        return QueryBuilders.termQuery("season", season);
    }

    public static TermQueryBuilder hostTeam(String teamName) {
        return QueryBuilders.termQuery("hostTeam", teamName);
    }

    public static TermQueryBuilder guestTeam(String teamName) {
        return QueryBuilders.termQuery("guestTeam", teamName);
    }

    public static RangeQueryBuilder hostGoals(int goals, RangeModes rangeMode) {
        return rangeMode.get(QueryBuilders.rangeQuery("hostGoals"), goals);
    }

    public static RangeQueryBuilder allGoals(int goals, RangeModes rangeMode) {
        return rangeMode.get(QueryBuilders.rangeQuery("allGoals"), goals);
    }

    public static RangeQueryBuilder guestGoals(int goals, RangeModes rangeMode) {
        return rangeMode.get(QueryBuilders.rangeQuery(SearchDictionary.GUEST_GOALS), goals);
    }

    public static BoolQueryBuilder guestOrHostTeam(String teamName) {
        return QueryBuilders.boolQuery()
                .should(QueryFilters.hostTeam(teamName))
                .should(QueryFilters.guestTeam(teamName));
    }

    public static TermsQueryBuilder seasonsIn(Collection<String> seasons) {
        return QueryBuilders.termsQuery("season", seasons);
    }

}
