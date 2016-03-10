package com.waitingforcode.elasticsearch.consumer.impl;

import com.waitingforcode.rest.dto.table.PlaceDto;
import com.waitingforcode.rest.dto.table.PlaceStatsDto;
import com.waitingforcode.rest.dto.table.TableDto;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TableConsumerImplTest {

    private static final String RC_LENS = "RC Lens";
    private static final String AC_AJACCIO = "AC Ajaccio";
    private static final String SEASON_0102 = "2001/2002";
    private static final String SEASON_0203 = "2002/2003";

    private TableConsumerImpl tableConsumer = new TableConsumerImpl();

    @Mock
    private SearchResponse response;

    @Mock
    private SearchHit place1, place2, place3;

    @Mock
    private SearchHits hits;

    private String place1Result = "1.RC Lens;90;45;35;30;25;20;15;25;30;35;40;45;60;60;60;60;60";
    private String place2Result = "5.AC Ajaccio;90;45;70;65;60;55;50;50;55;60;65;70;120;120;120;120;120";
    private String place3Result = "12.RC Lens;120;55;25;20;15;100;95;1;5;10;15;25;26;25;25;115;120";

    @Before
    public void init() {
        Map<String, Object> mapPlace1 =  getMap(1, RC_LENS, SEASON_0102, 90, 45, new Integer[]{35, 30, 25, 20, 15},
                new Integer[]{25, 30, 35, 40, 45});
        Map<String, Object> mapPlace2 = getMap(5, AC_AJACCIO, SEASON_0102, 90, 45, new Integer[]{70, 65, 60, 55, 50},
                new Integer[]{50, 55, 60, 65, 70});
        Map<String, Object> mapPlace3 = getMap(12, RC_LENS, SEASON_0203, 120, 55, new Integer[]{25, 20, 15, 100, 95},
                new Integer[]{1, 5, 10, 15, 25});
        when(response.getHits()).thenReturn(hits);
        when(place1.sourceAsMap()).thenReturn(mapPlace1);
        when(place2.sourceAsMap()).thenReturn(mapPlace2);
        when(place3.sourceAsMap()).thenReturn(mapPlace3);
    }

    @Test
    public void should_find_table_for_given_season() {
        when(hits.getHits()).thenReturn(new SearchHit[] {place1, place2});

        TableDto table = tableConsumer.getTableForSeason(response, SEASON_0102);

        List<String> gotPlaces = table.getPlaces().stream().map(new PlaceToStringMapper()).collect(Collectors.toList());
        assertThat((CharSequence) table.getSeason()).isEqualTo(SEASON_0102);
        assertThat(gotPlaces).containsOnly(place1Result, place2Result);
    }

    @Test
    public void should_find_team_places_in_seasons() {
        when(hits.getHits()).thenReturn(new SearchHit[] {place1, place3});

        List<PlaceDto> parisSgPlaces = tableConsumer.getPlacesByTeamForEachSeason(response);

        List<String> gotPlaces = parisSgPlaces.stream().map(new PlaceToStringMapper()).collect(Collectors.toList());
        assertThat(gotPlaces).containsOnly(place1Result, place3Result);
    }

    private static class PlaceToStringMapper implements Function<PlaceDto, String> {

        @Override
        public String apply(PlaceDto placeDto) {
            return new StringBuilder(""+placeDto.getPlace()).append(".").append(placeDto.getTeam()).append(";")
                    .append(placeDto.getPoints()).append(";").append(placeDto.getGames()).append(";")
                    .append(statsLine(placeDto.getHomeStats())).append(";").append(statsLine(placeDto.getAwayStats()))
                    .append(";").append(statsLine(placeDto.getAllStats())).toString();
        }

        private String statsLine(PlaceStatsDto statsDto) {
            return new StringBuilder("").append(statsDto.getWins()).append(";").append(statsDto.getDraws())
                    .append(";").append(statsDto.getLosses()).append(";").append(statsDto.getGoalsScored())
                    .append(";").append(statsDto.getGoalsConceded())
                    .toString();
        }
    }

    public static Map<String, Object> getMap(int place, String team, String season,
                            int points, int games,
                            Integer[] homeData, Integer[] awayData) {
        Map<String, Object> sourceMap = new HashMap<>();
        Map<String, Integer> homeStats = new HashMap<>();
        new HashMap<>();
        homeStats.put("wins", homeData[0]);
        homeStats.put("draws", homeData[1]);
        homeStats.put("losses", homeData[2]);
        homeStats.put("scored", homeData[3]);
        homeStats.put("conceded", homeData[4]);
        sourceMap.put("homeStats", homeStats);

        Map<String, Integer> awayStats = new HashMap<>();
        awayStats.put("wins", awayData[0]);
        awayStats.put("draws", awayData[1]);
        awayStats.put("losses", awayData[2]);
        awayStats.put("scored", awayData[3]);
        awayStats.put("conceded", awayData[4]);
        sourceMap.put("awayStats", awayStats);

        Map<String, Integer> allStats = new HashMap<>();
        allStats.put("wins", homeData[0]+awayData[0]);
        allStats.put("draws", homeData[1]+awayData[1]);
        allStats.put("losses", homeData[2]+awayData[2]);
        allStats.put("scored", homeData[3]+awayData[3]);
        allStats.put("conceded", homeData[4]+awayData[4]);
        sourceMap.put("allStats", allStats);

        sourceMap.put("place", place);
        sourceMap.put("team", team);
        sourceMap.put("points", points);
        sourceMap.put("games", games);
        sourceMap.put("season", season);

        return sourceMap;
    }

}
