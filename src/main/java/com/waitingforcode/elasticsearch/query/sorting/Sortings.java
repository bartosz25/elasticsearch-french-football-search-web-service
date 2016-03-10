package com.waitingforcode.elasticsearch.query.sorting;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

public final class Sortings {

    private Sortings() {
        // prevents init
    }

    /**
     * Generates sorting clause for ordering by goals scored by given team. Note the presence of filter builder in parameters.
     * As told in <a href="https://www.elastic.co/guide/en/elasticsearch/guide/current/nested-sorting.html">Elasticsearch nested sorting</a>,
     * defined filter ensures that only expected (= filter matching) documents will be returned.
     *
     * @param teamName
     * @param filterBuilder
     * @return {@code SortBuilder} to use in request builders.
     */
    public static SortBuilder scoredGoalsByTeamWithFilter(String teamName, QueryBuilder filterBuilder) {
        String sortingScript = "doc['hostTeam'].value.equals('" + teamName + "') ? doc['hostGoals'].value : doc['guestGoals'].value";

        return SortBuilders.scriptSort(new Script(sortingScript), "string")
                .setNestedFilter(filterBuilder);
    }

    /**
     * Generates sorting clause to order by adversary team name.
     *
     * @param myTeamName The name of my team
     * @return {@code SortBuilder} with script method to sort by adversary team name.
     */
    public static SortBuilder adversary(String myTeamName) {
        String sortingScript = "doc['hostTeam'].value.equals('" + myTeamName + "') ? doc['guestTeam'].value : doc['hostTeam'].value";

        return SortBuilders.scriptSort(new Script(sortingScript), "string");
    }

    /**
     * Creates default sorting clause to apply when two documents from the main sorting clause are equal (for example they
     * have the same number of scored goals when we sort by scored goals)
     *
     * @return {@code SortBuilder} which should be used for all queries.
     */
    public static SortBuilder defaultSecondSort() {
        return SortBuilders.fieldSort("_id").order(SortOrder.ASC);
    }

}
