package com.waitingforcode.rest.resources;

import com.waitingforcode.elasticsearch.consumer.ScoreConsumer;
import com.waitingforcode.elasticsearch.consumer.SeasonConsumer;
import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.service.ScoreService;
import com.waitingforcode.elasticsearch.service.TableService;
import com.waitingforcode.elasticsearch.util.Pagination;
import com.waitingforcode.rest.dto.general.CollectionResponseDto;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreStatsDto;
import com.waitingforcode.rest.dto.score.SeasonDto;
import com.waitingforcode.rest.dto.score.StatsDto;
import com.waitingforcode.rest.sort.AdversaryDtoSorting;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Path("/scores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScoresResource {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private TableService tableService;

    @Autowired
    private SeasonConsumer seasonConsumer;

    @Autowired
    private ScoreConsumer scoreConsumer;

    @GET
    @Path("teams/{team}/scored-goals/{goals}")
    public CollectionResponseDto<SeasonDto> getMatchesByScoredGoals(@PathParam("team") String teamName,
                                                                @PathParam("goals") int goals,
                                                                @BeanParam Pagination pagination) {
        CountableSearchResponse esResponse = scoreService.findMatchesByScoredGoalsForTeam(teamName, goals, pagination);
        CollectionResponseDto<SeasonDto> response = new CollectionResponseDto<>();
        response.setItems(seasonConsumer.getSeasonsFromResponse(esResponse.getDataResponse()));
        response.setAllElements(esResponse.getAllElements());
        return response;
    }

    @GET
    @Path("teams/{team}/scored-goals/won-games/{goals}")
    public CollectionResponseDto<SeasonDto> getWonMatchesByScoringAtLeastSomeGoals(@PathParam("team") String teamName,
                                                                                   @PathParam("goals") int goals,
                                                                                   @BeanParam Pagination pagination) {
        CountableSearchResponse esResponse = scoreService.findWonMatchesByScoredGoalsForTeam(teamName, goals, pagination);
        CollectionResponseDto<SeasonDto> response = new CollectionResponseDto<>();
        response.setItems(seasonConsumer.getSeasonsFromResponse(esResponse.getDataResponse()));
        response.setAllElements(esResponse.getAllElements());
        return response;
    }

    @GET
    @Path("teams/{team}/stats")
    public StatsDto getStatsByTeam(@PathParam("team") String teamName,
                                   @BeanParam Pagination pagination) {
        StatsDto statsDto = new StatsDto();

        SearchResponse maxGoalsResponse = scoreService.findMaxScoredGoalsForTeam(teamName);
        int maxScored = scoreConsumer.getMaxScoredGoals(maxGoalsResponse, teamName);

        // for learning purposes, we show InternalSum instead of picking these values directly in a field
        List<SearchResponse> resultWonResponses
                = scoreService.findGamesResultsByTeamAndScoredGoals(teamName, maxScored, ScoreTypes.WON);
        List<SearchResponse> resultDrawResponses
                = scoreService.findGamesResultsByTeamAndScoredGoals(teamName, maxScored, ScoreTypes.DRAW);
        List<SearchResponse> resultLostResponses
                = scoreService.findGamesResultsByTeamAndScoredGoals(teamName, maxScored, ScoreTypes.LOST);

        ScoreStatsDto wonGamesDto = scoreConsumer.getStatsForScoreType(resultWonResponses, ScoreTypes.WON);
        ScoreStatsDto drawGamesDto = scoreConsumer.getStatsForScoreType(resultDrawResponses, ScoreTypes.DRAW);
        ScoreStatsDto lostGamesDto = scoreConsumer.getStatsForScoreType(resultLostResponses, ScoreTypes.LOST);

        statsDto.addGame(wonGamesDto, drawGamesDto, lostGamesDto);

        CountableSearchResponse scoredGoalsResponse = scoreService.findAllScoredAndConcededGoalsForTeam(teamName, pagination);
        statsDto = scoreConsumer.getGoalsBySeason(scoredGoalsResponse.getDataResponse(), statsDto);

        return statsDto;
    }

    @GET
    @Path("teams/{team}/goal-series/{goals}")
    public Object getGoalSeriesForTeam(@PathParam("team") String teamName, @PathParam("goals") int goals,
                                       @BeanParam Pagination pagination) {
        // TODO : implement me
        // TODO : for this occassion, the query getting "series" gets in fact all plays where team scored at least X goals
        // Filter is made on Java level because Groovy scripts seems to be greedy <= write article about the costs of
        // Groovy scripting: http://java.dzone.com/articles/elasticsearch-one-tip-day-0


        return null;
    }
    @GET
    @Path("teams/{team}/most-popular")
    public Map<String, List<BaseScoreDto>> getMostPopularScoresByTeamGroupedBySeason(@PathParam("team") String teamName) {
        CountableSearchResponse scoresResponse = scoreService.findTheMostPopularScoresByTeam(teamName);
        return scoreConsumer.getMostPopularScored(scoresResponse.getDataResponse());
    }

    @GET
    @Path("teams/{team}/best-scorer-adversary")
    public CollectionResponseDto<AdversaryDto> getTeamScoredTheMostGoalsToAnotherTeam(@PathParam("team") String teamName) {
        CountableSearchResponse searchResponse = scoreService.findTeamsScoredTheMostGoalsAgainst(teamName);
        List<AdversaryDto> adversaries = scoreConsumer.getBestScorerAdversaries(searchResponse.getDataResponse());
        Collections.sort(adversaries, AdversaryDtoSorting.SCORED_GOALS_DESC);

        CollectionResponseDto<AdversaryDto> collectionResponseDto = new CollectionResponseDto<>();
        collectionResponseDto.setAllElements(searchResponse.getAllElements());
        collectionResponseDto.setItems(adversaries);
        return collectionResponseDto;
    }

    @GET
    @Path("hockey-scores")
    public Map<String, List<HockeyScoreDto>> getHockeyScoresByRoundsAndSeasons(@DefaultValue("6") @QueryParam("minGoals") int minGoals) {
        SearchResponse hockeyResponse = scoreService.findHockeyScoresByRoundsAndSeasons(minGoals);
        return scoreConsumer.getHockeyScores(hockeyResponse);
    }

    @GET
    @Path("teams/{team}/places/goals/{goals}/times/{times}")
    public CollectionResponseDto<PlacesByScoredGoalsDto> getPlacesAfterScoringGoalsSomeTimes(@PathParam("team") String teamName,
            @PathParam("goals") int goals, @PathParam("times") int times) {
        SearchResponse seasonsResponse = scoreService.findSeasonsForTeamScoredGoalsAtLeastSomeTimes(teamName,
                goals, times);
        Map<String, Long> seasons = seasonConsumer.getSeasonsWithDocOccurrence(seasonsResponse);

        SearchResponse placesResponse = tableService.findTeamPlaceInSeasons(teamName, seasons.keySet());

        CollectionResponseDto<PlacesByScoredGoalsDto> responseDto = new CollectionResponseDto<>();
        responseDto.setItems(scoreConsumer.getPlacesBySeason(placesResponse, seasons));
        return responseDto;
    }

}
