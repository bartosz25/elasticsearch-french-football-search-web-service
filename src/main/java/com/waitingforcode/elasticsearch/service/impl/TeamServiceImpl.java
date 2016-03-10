package com.waitingforcode.elasticsearch.service.impl;


import com.waitingforcode.elasticsearch.config.IndexConfig;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.service.TeamService;
import com.waitingforcode.elasticsearch.util.Pagination;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private IndexConfig index;

    @Override
    public CountableSearchResponse findAllTeams(Pagination pagination) {
        return new CountableSearchResponse(index.teams()
                .addSort("name", SortOrder.ASC)
                .setFrom(pagination.getFrom())
                .setSize(pagination.getPerPage())
                .get());
    }

    @Override
    public CountableSearchResponse findAllTeamsByName(String name, Pagination pagination) {
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("name:"+name)
                //.analyzer("team_synonym_analyzer")
                .queryName("teamSynonymName");

        return new CountableSearchResponse(index.teams()
                .setQuery(queryBuilder)
                .setFrom(pagination.getFrom())
                .setSize(pagination.getPerPage())
                .addSort("name", SortOrder.ASC)
                .get());
    }

    @Override
    public CountableSearchResponse findAllTeamsByNameAndFuzziness(String name, Pagination pagination) {
        QueryBuilder queryBuilder = QueryBuilders.fuzzyQuery("name", name)
                .fuzziness(Fuzziness.ONE)
                .queryName("fuzzyTeamName");

        return new CountableSearchResponse(index.teams()
                .setQuery(queryBuilder)
                .setFrom(pagination.getFrom())
                .setSize(pagination.getPerPage())
                .addSort("name", SortOrder.ASC)
                .get());
    }
}
