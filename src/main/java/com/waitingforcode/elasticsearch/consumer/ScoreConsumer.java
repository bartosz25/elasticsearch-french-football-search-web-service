package com.waitingforcode.elasticsearch.consumer;

import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreStatsDto;
import com.waitingforcode.rest.dto.score.StatsDto;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;
import java.util.Map;

public interface ScoreConsumer {

    int getMaxScoredGoals(SearchResponse response, String teamName);

    ScoreStatsDto getStatsForScoreType(List<SearchResponse> responses, ScoreTypes scoreType);

    StatsDto getGoalsBySeason(SearchResponse response, StatsDto scoreStats);

    Map<String, List<BaseScoreDto>> getMostPopularScored(SearchResponse searchResponse);

    List<AdversaryDto> getBestScorerAdversaries(SearchResponse searchResponse);

    Map<String, List<HockeyScoreDto>> getHockeyScores(SearchResponse searchResponse);

    List<PlacesByScoredGoalsDto> getPlacesBySeason(SearchResponse searchResponse, Map<String, Long> seasons);
}
