package com.waitingforcode.rest.dto.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlaceDto {

    private String season;

    private int place;

    private String team;

    private int games;

    private int points;

    private PlaceStatsDto allStats;

    private PlaceStatsDto homeStats;

    private PlaceStatsDto awayStats;

    private PlaceDto(int place, String team, int games, int points, PlaceStatsDto allStats,
                     PlaceStatsDto homeStats, PlaceStatsDto awayStats, String season) {
        this.place = place;
        this.team = team;
        this.games = games;
        this.points = points;
        this.allStats = allStats;
        this.homeStats = homeStats;
        this.awayStats = awayStats;
        this.season = season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSeason() {
        return season;
    }

    public int getPlace() {
        return place;
    }

    public String getTeam() {
        return team;
    }

    public int getGames() {
        return games;
    }

    public int getPoints() {
        return points;
    }

    public PlaceStatsDto getAllStats() {
        return allStats;
    }

    public PlaceStatsDto getHomeStats() {
        return homeStats;
    }

    public PlaceStatsDto getAwayStats() {
        return awayStats;
    }

    public static class Builder {
        private int place;
        private String team;
        private int games;
        private int points;
        private PlaceStatsDto allStats;
        private PlaceStatsDto homeStats;
        private PlaceStatsDto awayStats;
        private String season;

        public Builder forPlaceAndTeam(int place, String team) {
            this.place = place;
            this.team = team;
            return this;
        }

        public Builder withPointsOnGames(int points, int games) {
            this.points = points;
            this.games = games;
            return this;
        }

        public Builder withStats(PlaceStatsDto stats, PlaceStatsDto.Types type) {
            if (type == PlaceStatsDto.Types.ALL) {
                allStats = stats;
            } else if (type == PlaceStatsDto.Types.HOME) {
                homeStats = stats;
            } else {
                awayStats = stats;
            }
            return this;
        }

        public Builder andSeason(String season) {
            this.season = season;
            return this;
        }

        public PlaceDto build() {
            return new PlaceDto(place, team, games, points, allStats, homeStats, awayStats, season);
        }

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("place", place).add("team", team).add("games", games)
                .add("points", points).add("allStats", allStats).add("homeStats", homeStats).add("awayStats", awayStats)
                .toString();
    }

}
