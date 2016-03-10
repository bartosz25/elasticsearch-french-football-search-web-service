package com.waitingforcode.rest.converter;


import com.google.common.base.Preconditions;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreDto;
import com.waitingforcode.rest.dto.score.SeasonDto;
import com.waitingforcode.rest.dto.table.PlaceDto;
import com.waitingforcode.rest.dto.table.PlaceStatsDto;
import com.waitingforcode.rest.dto.team.TeamDto;
import org.elasticsearch.search.SearchHit;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;
import java.util.function.Function;

public abstract class SearchHitConverters {

    public static Converter<SearchHit, TeamDto> toTeam() {
        return ToTeamConverter.INSTANCE;
    }

    public static Converter<SearchHit, ScoreDto> toScore() {
        return ToScoreConverter.INSTANCE;
    }

    public static Converter<SearchHit, SeasonDto> toSeason() {
        return ToSeasonConverter.INSTANCE;
    }

    public static PlacesByScoredGoalsDto toPlacesByScoredGoals(SearchHit searchHit, int timesScored) {
        Map<String, Object> data = searchHit.sourceAsMap();
        return new PlacesByScoredGoalsDto((String)data.get(SearchDictionary.SEASON), (Integer)data.get(SearchDictionary.PLACE),
                timesScored);
    }

    public static Converter<SearchHit, PlaceDto> toPlace() {
        return ToPlaceConverter.INSTANCE;
    }

    public static Function<SearchHit, PlacesByScoredGoalsDto> toPlacesConverter(Map<String, Long> placesBySeason) {
        return new ToPlacesConverter(placesBySeason);
    }

    private static class ToPlacesConverter implements Function<SearchHit, PlacesByScoredGoalsDto> {

        private Map<String, Long> placesBySeason;

        public ToPlacesConverter(Map<String, Long> placesBySeason) {
            this.placesBySeason = placesBySeason;
        }

        @Override
        public PlacesByScoredGoalsDto apply(SearchHit searchHit) {
            String season = (String) searchHit.sourceAsMap().get(SearchDictionary.SEASON);
            Long scoringTimes = placesBySeason.get(season);
            Preconditions.checkNotNull(scoringTimes);
            return SearchHitConverters.toPlacesByScoredGoals(searchHit, scoringTimes.intValue());
        }
    }

    private enum ToTeamConverter implements Converter<SearchHit, TeamDto> {
        INSTANCE {
            @Override
            public TeamDto convert(SearchHit searchHit) {
                Map<String, Object> data = searchHit.sourceAsMap();
                return TeamDto.valueOf((String) data.get(SearchDictionary.NAME));
            }
        }
    }

    private enum ToScoreConverter implements Converter<SearchHit, ScoreDto> {
        INSTANCE {
            @Override
            public ScoreDto convert(SearchHit searchHit) {
                Map<String, Object> data = searchHit.sourceAsMap();
                ScoreDto score = new ScoreDto();
                score.setHostTeam(TeamDto.valueOf((String) data.get(SearchDictionary.HOST_TEAM)));
                score.setGuestTeam(TeamDto.valueOf((String) data.get(SearchDictionary.GUEST_TEAM)));
                score.setRound((Integer) data.get(SearchDictionary.ROUND));
                score.setHostGoals((Integer) data.get(SearchDictionary.HOST_GOALS));
                score.setGuestGoals((Integer) data.get(SearchDictionary.GUEST_GOALS));
                return score;
            }
        }
    }

    private enum ToSeasonConverter implements Converter<SearchHit, SeasonDto> {
        INSTANCE {
            @Override
            public SeasonDto convert(SearchHit searchHit) {
                Map<String, Object> data = searchHit.sourceAsMap();
                return SeasonDto.valueOf((String) data.get(SearchDictionary.SEASON));
            }
        }
    }

    private enum ToPlaceConverter implements Converter<SearchHit, PlaceDto> {
        INSTANCE {
            @Override
            public PlaceDto convert(SearchHit searchHit) {
                Map<String, Object> data = searchHit.sourceAsMap();
                Map<String, Integer> allStats = (Map<String, Integer>) data.get("allStats");
                Map<String, Integer> homeStats = (Map<String, Integer>) data.get("homeStats");
                Map<String, Integer> awayStats = (Map<String, Integer>) data.get("awayStats");


                PlaceStatsDto statsAll = new PlaceStatsDto.Builder()
                        .withWinsDrawsAndLosses(allStats.get(SearchDictionary.WINS), allStats.get(SearchDictionary.DRAWS), 
                                allStats.get(SearchDictionary.LOSSES))
                        .forScoredAndConcededGoals(allStats.get(SearchDictionary.SCORED), allStats.get(SearchDictionary.CONCEDED))
                        .build();
                PlaceStatsDto statsHome = new PlaceStatsDto.Builder()
                        .withWinsDrawsAndLosses(homeStats.get(SearchDictionary.WINS), homeStats.get(SearchDictionary.DRAWS), homeStats.get(SearchDictionary.LOSSES))
                        .forScoredAndConcededGoals(homeStats.get(SearchDictionary.SCORED), homeStats.get(SearchDictionary.CONCEDED))
                        .build();
                PlaceStatsDto statsAway = new PlaceStatsDto.Builder()
                        .withWinsDrawsAndLosses(awayStats.get(SearchDictionary.WINS), awayStats.get(SearchDictionary.DRAWS), awayStats.get(SearchDictionary.LOSSES))
                        .forScoredAndConcededGoals(awayStats.get(SearchDictionary.SCORED), awayStats.get(SearchDictionary.CONCEDED))
                        .build();

                return new PlaceDto.Builder().forPlaceAndTeam((Integer) data.get(SearchDictionary.PLACE), (String) data.get(SearchDictionary.TEAM))
                        .withPointsOnGames((Integer) data.get(SearchDictionary.POINTS), (Integer) data.get(SearchDictionary.GAMES))
                        .withStats(statsAll, PlaceStatsDto.Types.ALL)
                        .withStats(statsHome, PlaceStatsDto.Types.HOME)
                        .withStats(statsAway, PlaceStatsDto.Types.AWAY)
                        .andSeason((String) data.get(SearchDictionary.SEASON))
                        .build();
            }
        }
    }

}
