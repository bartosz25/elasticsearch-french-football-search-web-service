package com.waitingforcode.elasticsearch.service;


import org.elasticsearch.action.search.SearchResponse;

import java.util.Collection;

public interface TableService {

    SearchResponse findTeamPlaceInSeasons(String teamName, Collection<String> seasons);

    SearchResponse findTableBySeason(String season);

    SearchResponse findPlacesByTeam(String teamName);

}
