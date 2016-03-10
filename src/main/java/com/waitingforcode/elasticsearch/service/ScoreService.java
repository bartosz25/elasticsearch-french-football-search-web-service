package com.waitingforcode.elasticsearch.service;

import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.util.Pagination;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;

public interface ScoreService {

    /**
     * Searches matches when given team scored at least X goals.
     *
     * For example, we have 3 matches:
     * <ol>
     *     <li>RC Lens - Lille OSC 4:2</li>
     *     <li>RC Lens - AC Ajaccio 2:1</li>
     *     <li>Paris-SG - RC Lens 2:2</li>
     * </ol>
     *
     * If we search matches of <em>RC Lens</em> where this team scored at least 2 goals, we'll receive all 3 matches. But if we
     * are interested only by matches where 3 or more goals were scored, only the first document will be returned.
     *
     * </pre>
     *
     * @param teamName The name of team which scores we want to search
     * @param scoredGoals Minimum number of goals expected to be scored by team identified by {@code teamName}
     * @param pagination Pagination to use in returned response.
     * @return {@code CountableSearchResponse} containing hit data and all matching documents counter.
     */
    CountableSearchResponse findMatchesByScoredGoalsForTeam(String teamName, int scoredGoals, Pagination pagination);

    CountableSearchResponse findWonMatchesByScoredGoalsForTeam(String teamName, int scoredGoals, Pagination pagination);

    CountableSearchResponse findAllScoredAndConcededGoalsForTeam(String teamName, Pagination pagination);

    /**
     * Finds all "hockey" scores. "Hockey" score is identified by the minimum number of goals scored by both, guest and host,
     * teams in given match.
     *
     * For example:
     * If we define that "hockey" score begins from 5 goals, the document representing this score 4:1 will be returned but
     * the one of 2:2 not.
     *
     * @param minHockeyGoals Minimum number of goals to consider score as "hockey"
     * @return {@code SearchResponse} with all aggregations representing hockey scores stats. There are no pagination, so
     *         the result should be contained only in aggregations.
     */
    SearchResponse findHockeyScoresByRoundsAndSeasons(int minHockeyGoals);

    CountableSearchResponse findTheMostPopularScoresByTeam(String teamName);

    /**
     * Gets teams which scored the most goals against {@code teamName}
     *
     * @param teamName
     * @return {@code CountableSearchResponse} composed by aggregation results of the teams scored the most goals against
     *         {@code teamName}. As pagination is not envisaged, the implementation should not return hits.
     */
    CountableSearchResponse findTeamsScoredTheMostGoalsAgainst(String teamName);

    SearchResponse findSeasonsForTeamScoredGoalsAtLeastSomeTimes(String teamName, int scoredGoals, int times);

    /**
     * Retrieves the max number of goals scored by given team in all seasons.
     *
     * @param teamName Name of the team which max number of scored goals we want to find.
     * @return {@code SearchResponse} containing the information about the max number of goals scored by given team. The query
     *         should be aggregation-based only.
     */
    SearchResponse findMaxScoredGoalsForTeam(String teamName);

    /**
     *
     * @param teamName
     * @param maxScoredGoals Max scored goals in all seasons
     * @param scoreType Type of game (won, lost, draw)
     * @return
     */
    List<SearchResponse> findGamesResultsByTeamAndScoredGoals(String teamName, int maxScoredGoals, ScoreTypes scoreType);

}
