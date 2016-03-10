package com.waitingforcode.rest.resources;

import com.google.common.collect.Lists;
import com.waitingforcode.configuration.ServletContainerConfigurationRunner;
import com.waitingforcode.rest.dto.general.CollectionResponseDto;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreStatsDto;
import com.waitingforcode.rest.dto.score.StatsDto;
import com.waitingforcode.util.Indexers;
import com.waitingforcode.util.ScoreInput;
import com.waitingforcode.util.TableInput;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ServletContainerConfigurationRunner.class})
@WebIntegrationTest
@ActiveProfiles( profiles = {"test"})
public class ScoresResourceIntegrationTest {

    private static final String PARIS_SG = "paris-sg";
    private static final String LILLE_OSC = "lille osc";
    private static final String AS_MONACO = "as monaco";
    private static final String AJ_AUXERRE = "aj auxerre";
    private static final String OLYMPIQUE_LYON = "olympique lyon";
    private static final String RC_LENS = "rc lens";
    private static final String SEASON_0102 = "2001/2002";
    private static final String SEASON_0203 = "2002/2003";

    @Autowired
    private Indexers indexers;

    private RestTemplate restTemplate = new TestRestTemplate();

    @After
    public void reset() {
        indexers.cleanAll("scores", "tables");
    }

    @Test
    public void should_get_all_stats_by_team() throws URISyntaxException {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, LILLE_OSC).withGoals(2, 1);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(1, 1).build();
        ScoreInput score3 = builder.withTeams(PARIS_SG, AS_MONACO).withGoals(1, 0).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, AJ_AUXERRE).withGoals(1, 0).build();
        ScoreInput score5 = builder.withTeams(PARIS_SG, AJ_AUXERRE).withGoals(3, 4).build();
        ScoreInput score6 = builder.withTeams(PARIS_SG, RC_LENS).withGoals(2, 0).build();
        ScoreInput score7 = builder.forSeason(SEASON_0203).withTeams(LILLE_OSC, PARIS_SG).withGoals(1, 5).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score7));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<StatsDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/teams/paris-sg/stats"), StatsDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StatsDto statsDto = response.getBody();
        assertThat(statsDto.getGoals()).hasSize(2);
        assertThat(statsDto.getGoals().get(SEASON_0102).getConcededAway()).isEqualTo(1);
        assertThat(statsDto.getGoals().get(SEASON_0102).getConcededHome()).isEqualTo(5);
        assertThat(statsDto.getGoals().get(SEASON_0102).getScoredAway()).isEqualTo(1);
        assertThat(statsDto.getGoals().get(SEASON_0102).getScoredHome()).isEqualTo(9);
        assertThat(statsDto.getGoals().get(SEASON_0203).getConcededAway()).isEqualTo(1);
        assertThat(statsDto.getGoals().get(SEASON_0203).getConcededHome()).isEqualTo(0);
        assertThat(statsDto.getGoals().get(SEASON_0203).getScoredAway()).isEqualTo(5);
        assertThat(statsDto.getGoals().get(SEASON_0203).getScoredHome()).isEqualTo(0);
        assertThat(matchesFromType("WON", statsDto.getGames())).extracting((match) -> match.getGames() + "/" + match.getGoals())
                .containsOnly("0/0", "2/1", "2/2", "0/3", "0/4", "1/5");
        assertThat(matchesFromType("DRAW", statsDto.getGames())).extracting((match) -> match.getGames()+"/"+match.getGoals())
                .containsOnly("0/0", "1/1", "0/2", "0/3", "0/4", "0/5");
        assertThat(matchesFromType("LOST", statsDto.getGames())).extracting((match) -> match.getGames() + "/" + match.getGoals())
                .containsOnly("0/0", "0/1", "0/2", "1/3", "0/4", "0/5");
    }

    @Test
    public void should_find_hockey_scores() throws URISyntaxException {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, LILLE_OSC).withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(0, 3).build();
        ScoreInput score3 = builder.withTeams(AJ_AUXERRE, PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, AJ_AUXERRE).withGoals(2, 2).build();
        ScoreInput score5 = builder.playedInRound(2).withTeams(PARIS_SG, AJ_AUXERRE).withGoals(5, 5).build();
        ScoreInput score6 = builder.playedInRound(3).withTeams(PARIS_SG, OLYMPIQUE_LYON).withGoals(3, 3).build();
        ScoreInput score7 = builder.forSeason(SEASON_0203).withTeams(LILLE_OSC, PARIS_SG).withGoals(1, 5).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score7));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/hockey-scores?minGoals=2"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, List<HockeyScoreDto>> hockeyScores = response.getBody();
        assertThat(hockeyScores).hasSize(2);
        assertThat(hockeyScores).containsKeys(SEASON_0102, SEASON_0203);
        assertThat(hockeyScores.get(SEASON_0102)).extracting("round").containsOnly(1, 2, 3);
        assertThat(hockeyScores.get(SEASON_0102)).extracting("games").containsOnly(4, 1, 1);
        assertThat(hockeyScores.get(SEASON_0203)).extracting("round").containsOnly(3);
        assertThat(hockeyScores.get(SEASON_0203)).extracting("games").containsOnly(1);
    }

    @Test
    public void should_find_the_best_adversaries_of_paris_sg() throws URISyntaxException {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, LILLE_OSC).withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(0, 3).build();
        ScoreInput score3 = builder.withTeams(AJ_AUXERRE, PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, AS_MONACO).withGoals(2, 2).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/teams/paris-sg/best-scorer-adversary"),
                        CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<AdversaryDto> adversaries = response.getBody().getItems();
        assertThat(adversaries).hasSize(3);
        assertThat(adversaries).extracting("team").containsExactly(AJ_AUXERRE, AS_MONACO, LILLE_OSC);
        assertThat(adversaries).extracting("goals").containsExactly(3, 2, 0);
        assertThat(adversaries).extracting("scoredAsHost").containsExactly(3, 0, 0);
        assertThat(adversaries).extracting("scoredAsGuest").containsExactly(0, 2, 0);
    }

    @Test
    public void should_find_2_matches_per_season_where_paris_sg_won_by_scoring_3_goals() throws URISyntaxException {
        String[] seasons = new String[] {SEASON_0102, SEASON_0203};
        int adder = 3;
        for (String season : seasons) {
            ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(season).playedInRound(1)
                    .withTeams(PARIS_SG, LILLE_OSC).withGoals(3+adder, 0);
            ScoreInput score1 = builder.build();
            ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(0, 1).build();
            ScoreInput score3 = builder.withTeams(AJ_AUXERRE, PARIS_SG).withGoals(5, 4).build();
            ScoreInput score4 = builder.withTeams(PARIS_SG, AS_MONACO).withGoals(1+adder, 2).build();
            indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4));
            adder--;
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/teams/paris-sg/scored-goals/won-games/3"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> seasonsDto =  response.getBody().getItems();
        assertThat(seasonsDto).hasSize(2);
        assertThat(seasonsDto).extracting("season").containsOnly(SEASON_0102, SEASON_0203);
        assertThat(findMatches(seasonsDto, SEASON_0102)).extracting("hostTeam").extracting("name").containsOnly(PARIS_SG);
        assertThat(findMatches(seasonsDto, SEASON_0102)).extracting("guestTeam").extracting("name").containsOnly(LILLE_OSC, AS_MONACO);
        assertThat(findMatches(seasonsDto, SEASON_0102)).extracting("hostGoals").containsOnly(6, 4);
        assertThat(findMatches(seasonsDto, SEASON_0102)).extracting("guestGoals").containsOnly(0, 2);
        assertThat(findMatches(seasonsDto, SEASON_0203)).extracting("hostTeam").extracting("name").containsOnly(PARIS_SG);
        assertThat(findMatches(seasonsDto, SEASON_0203)).extracting("guestTeam").extracting("name").containsOnly(LILLE_OSC, AS_MONACO);
        assertThat(findMatches(seasonsDto, SEASON_0203)).extracting("hostGoals").containsOnly(5, 3);
        assertThat(findMatches(seasonsDto, SEASON_0203)).extracting("guestGoals").containsOnly(0, 2);
    }

    @Test
    public void should_find_3_matches_where_parisg_sg_scored_3_goals() throws URISyntaxException {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
            .withTeams(PARIS_SG, LILLE_OSC).withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(0, 1).build();
        ScoreInput score3 = builder.withTeams(AJ_AUXERRE, PARIS_SG).withGoals(5, 3).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, AS_MONACO).withGoals(3, 2).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4));


        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/teams/paris-sg/scored-goals/3"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> matches = response.getBody().getItems();
        assertThat(matches).hasSize(1);
        assertThat(findMatches(matches, SEASON_0102)).extracting("hostTeam").extracting("name").containsOnly(PARIS_SG, AJ_AUXERRE);
        assertThat(findMatches(matches, SEASON_0102)).extracting("guestTeam").extracting("name").containsOnly(LILLE_OSC, AS_MONACO, PARIS_SG);
        assertThat(findMatches(matches, SEASON_0102)).extracting("hostGoals").containsOnly(5, 3);
        assertThat(findMatches(matches, SEASON_0102)).extracting("guestGoals").containsOnly(0, 3, 2);
    }

    @Test
    public void should_find_the_most_popular_scores_for_paris_sg_and_2_seasons() throws URISyntaxException {
        String[] seasons = new String[] {SEASON_0102, SEASON_0203};
        for (String season : seasons) {
            ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(season).playedInRound(1)
                    .withTeams(PARIS_SG, LILLE_OSC).withGoals(3, 0);
            ScoreInput score1 = builder.build();
            ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(0, 3).build();
            ScoreInput score3 = builder.withTeams(AJ_AUXERRE, PARIS_SG).withGoals(5, 4).build();
            ScoreInput score4 = builder.withTeams(PARIS_SG, AS_MONACO).withGoals(1, 2).build();
            ScoreInput score5 = builder.withTeams(PARIS_SG, RC_LENS).withGoals(1, 2).build();
            indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5));
        }
        ResponseEntity<Map> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/teams/paris-sg/most-popular"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, List<BaseScoreDto>> mostPopularScores = response.getBody();
        assertThat(mostPopularScores).hasSize(2);
        assertThat(mostPopularScores.get(SEASON_0102)).hasSize(4);
        assertThat(mostPopularScores.get(SEASON_0102)).extracting("score").containsExactly("1:2", "0:3", "3:0", "5:4");
        assertThat(mostPopularScores.get(SEASON_0203)).hasSize(4);
        assertThat(mostPopularScores.get(SEASON_0203)).extracting("score").containsExactly("1:2", "0:3", "3:0", "5:4");
    }

    @Test
    public void should_find_places_taken_by_paris_sg_after_scoring_2_goals_at_least_3_times() throws URISyntaxException {
        ScoreInput.Builder builder = new ScoreInput.Builder().forSeason(SEASON_0102).playedInRound(1)
                .withTeams(PARIS_SG, LILLE_OSC).withGoals(5, 0);
        ScoreInput score1 = builder.build();
        ScoreInput score2 = builder.withTeams(LILLE_OSC, PARIS_SG).withGoals(0, 3).build();
        ScoreInput score3 = builder.withTeams(AJ_AUXERRE, PARIS_SG).withGoals(3, 1).build();
        ScoreInput score4 = builder.withTeams(PARIS_SG, AS_MONACO).withGoals(2, 2).build();
        ScoreInput score5 = builder.forSeason(SEASON_0203).withGoals(5, 0).build();
        ScoreInput score6 = builder.forSeason(SEASON_0203).withGoals(3, 0).build();
        ScoreInput score7 = builder.forSeason(SEASON_0203).withGoals(3, 0).build();
        ScoreInput score8 = builder.forSeason(SEASON_0203).withGoals(4, 0).build();
        indexers.scoresFromObject(Lists.newArrayList(score1, score2, score3, score4, score5, score6, score7, score8));
        TableInput.TableInputBuilder tableBuilder = new TableInput.TableInputBuilder();
        TableInput season0102 = tableBuilder.forTeam(PARIS_SG).forSeason(SEASON_0102).andPlace(3)
                .withHomeStats(new TableInput.StatsBuilder().games(15, 4, 1).goals(59, 10).build())
                .withAwayStats(new TableInput.StatsBuilder().games(10, 9, 1).goals(50, 15).build())
                .build();
        TableInput season0203 = tableBuilder.forTeam(PARIS_SG).forSeason(SEASON_0203).andPlace(6).build();
        indexers.tablesFromObjects(Lists.newArrayList(season0102, season0203));

        // index values
        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/scores/teams/paris-sg/places/goals/2/times/3"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<PlacesByScoredGoalsDto> placesDto = response.getBody().getItems();
        assertThat(placesDto).hasSize(2);
        assertThat(placesDto).extracting("season").containsExactly(SEASON_0102, SEASON_0203);
        assertThat(placesDto).extracting("place").containsExactly(3, 6);
        assertThat(placesDto).extracting("times").containsExactly(3, 4);
    }


    private List<Map<String, Object>> findMatches(List<Map<String, Object>> seasons, String expectedSeason) {
        for (Map<String, Object> entry : seasons) {
            if (entry.get("season").equals(expectedSeason)) {
                return (List<Map<String, Object>>) entry.get("scores");
            }
        }
        return Collections.emptyList();
    }

    private List<ScoreStatsDto.Match> matchesFromType(String type, List<ScoreStatsDto> stats) {
        for (ScoreStatsDto stat : stats) {
            if (stat.getType().name().equals(type)) {
                return stat.getScores();
            }
        }
        return null;
    }
}
