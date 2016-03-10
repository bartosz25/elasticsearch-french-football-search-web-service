package com.waitingforcode.elasticsearch.data;

import com.waitingforcode.elasticsearch.query.filters.QueryFilters;
import com.waitingforcode.elasticsearch.query.filters.RangeModes;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public enum ScoreTypes {
    WON {
        @Override
        public QueryBuilder getScoreFilter(String teamName, int scoredGoals) {
            return QueryBuilders.boolQuery().should(
                    QueryBuilders.boolQuery().must(QueryFilters.hostTeam(teamName))
                    .must(QueryFilters.hostGoals(scoredGoals, RangeModes.EQ))
                    .must(QueryFilters.guestGoals(scoredGoals, RangeModes.LT))
            ).should(
                    QueryBuilders.boolQuery().must(QueryFilters.guestTeam(teamName))
                            .must(QueryFilters.guestGoals(scoredGoals, RangeModes.EQ))
                            .must(QueryFilters.hostGoals(scoredGoals, RangeModes.LT))
            );
        }
    },
    DRAW {
        @Override
        public QueryBuilder getScoreFilter(String teamName, int scoredGoals) {
            return QueryFilters.guestOrHostTeam(teamName)
                    .must(QueryFilters.guestGoals(scoredGoals, RangeModes.EQ))
                    .must(QueryFilters.hostGoals(scoredGoals, RangeModes.EQ));
        }
    },
    LOST {
        @Override
        public QueryBuilder getScoreFilter(String teamName, int scoredGoals) {
            return QueryBuilders.boolQuery().should(
                    QueryBuilders.boolQuery().must(QueryFilters.hostTeam(teamName))
                            .must(QueryFilters.guestGoals(scoredGoals, RangeModes.GT))
                            .must(QueryFilters.hostGoals(scoredGoals, RangeModes.EQ))
            ).should(
                    QueryBuilders.boolQuery().must(QueryFilters.guestTeam(teamName))
                            .must(QueryFilters.hostGoals(scoredGoals, RangeModes.GT))
                            .must(QueryFilters.guestGoals(scoredGoals, RangeModes.EQ))
            );
        }
    };

    public abstract QueryBuilder getScoreFilter(String teamName, int scoredGoals);
}
