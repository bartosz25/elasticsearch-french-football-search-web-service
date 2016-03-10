package com.waitingforcode.elasticsearch.service;


import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.util.Pagination;

public interface TeamService {

    CountableSearchResponse findAllTeams(Pagination pagination);

    CountableSearchResponse findAllTeamsByName(String name, Pagination pagination);

    CountableSearchResponse findAllTeamsByNameAndFuzziness(String name, Pagination pagination);

}
