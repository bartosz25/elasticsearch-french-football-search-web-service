package com.waitingforcode.elasticsearch.service.impl;

import com.waitingforcode.elasticsearch.config.ElasticSearchConfig;
import com.waitingforcode.elasticsearch.config.IndexConfig;
import com.waitingforcode.elasticsearch.query.filters.QueryFilters;
import com.waitingforcode.elasticsearch.service.TableService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private IndexConfig index;

    @Override
    public SearchResponse findTeamPlaceInSeasons(String teamName, Collection<String> seasons) {
        QueryBuilder filterBuilder =
                QueryBuilders.boolQuery()
                .must(QueryFilters.team(teamName))
                .must(QueryFilters.seasonsIn(seasons));

        return index.tables()
                .setQuery(QueryBuilders.boolQuery().must(filterBuilder))
                .addSort("season", SortOrder.ASC)
                .setFrom(0)
                .setSize(ElasticSearchConfig.DEFAULT_MAX_RESULTS)
                .get();
    }

    @Override
    public SearchResponse findTableBySeason(String season) {
        return index.tables()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.season(season)))
                .addSort("place", SortOrder.ASC)
                .setFrom(0)
                .setSize(ElasticSearchConfig.DEFAULT_MAX_RESULTS)
                .setExplain(true)
                .get();
    }

    /**
     * @param teamName Name of searched team.
     * @return {@code SearchResponse} with all places (from all indexed seasons). There are no reason to  paginate because
     *         the number of seasons is not very big.
     */
    @Override
    public SearchResponse findPlacesByTeam(String teamName) {
        return index.tables()
                .setQuery(QueryBuilders.boolQuery().must(QueryFilters.team(teamName)))
                .addSort("season", SortOrder.ASC)
                .setFrom(0)
                .setSize(ElasticSearchConfig.DEFAULT_MAX_RESULTS)
                .get();
    }

}
