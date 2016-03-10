package com.waitingforcode.util;

public class TableInput {

    private String season;
    private String team;
    private Integer place;
    private Integer points;
    private Integer games;
    private Stats homeStats;
    private Stats awayStats;
    private Stats allStats;

    private TableInput(String season, String team, Integer place, Stats homeStats, Stats awayStats) {
        this.season = season;
        this.team = team;
        this.place = place;
        this.homeStats = homeStats;
        this.awayStats = awayStats;
        this.allStats = concatStats(homeStats, awayStats);
        this.points = this.allStats.getPoints();
        this.games = this.allStats.getGames();
    }

    private Stats concatStats(Stats stats1, Stats stats2) {
        return new Stats(
                stats1.wins + stats2.wins,
                stats1.draws + stats2.draws,
                stats1.losses + stats2.losses,
                stats1.scored + stats2.scored,
                stats1.conceded + stats2.conceded
        );
    }

    public String getSeason() {
        return season;
    }

    public String getTeam() {
        return team;
    }

    public Integer getPlace() {
        return place;
    }

    public Integer getPoints() {
        return points;
    }

    public Integer getGames() {
        return games;
    }

    public Stats getHomeStats() {
        return homeStats;
    }

    public Stats getAwayStats() {
        return awayStats;
    }

    public Stats getAllStats() {
        return allStats;
    }

    public static class Stats {
        private static final int WINS_POINTS = 3;
        private static final int DRAW_POINTS = 1;

        private Integer games;
        private Integer points;
        private Integer wins;
        private Integer draws;
        private Integer losses;
        private Integer scored;
        private Integer conceded;

        public Stats(Integer wins, Integer draws, Integer losses, Integer scored, Integer conceded) {
            this.wins = wins;
            this.draws = draws;
            this.losses = losses;
            this.scored = scored;
            this.conceded = conceded;
            this.games = wins + draws + losses;
            this.points = (WINS_POINTS * wins) + (DRAW_POINTS + draws);
        }

        public Integer getGames() {
            return games;
        }

        public Integer getPoints() {
            return points;
        }

        public Integer getWins() {
            return wins;
        }

        public Integer getDraws() {
            return draws;
        }

        public Integer getLosses() {
            return losses;
        }

        public Integer getScored() {
            return scored;
        }

        public Integer getConceded() {
            return conceded;
        }
    }

    public static class StatsBuilder {
        private Integer wins;
        private Integer draws;
        private Integer losses;
        private Integer scored;
        private Integer conceded;

        public StatsBuilder games(Integer wins, Integer draws, Integer losses) {
            this.wins = wins;
            this.draws = draws;
            this.losses = losses;
            return this;
        }

        public StatsBuilder goals(Integer scored, Integer conceded) {
            this.scored = scored;
            this.conceded = conceded;
            return this;
        }

        public Stats build() {
            return new Stats(wins, draws, losses, scored, conceded);
        }
    }

    public static class TableInputBuilder {
        private String season;
        private String team;
        private Integer place;
        private Stats homeStats;
        private Stats awayStats;

        public TableInputBuilder forSeason(String season) {
            this.season = season;
            return this;
        }

        public TableInputBuilder forTeam(String team) {
            this.team = team;
            return this;
        }

        public TableInputBuilder andPlace(Integer place) {
            this.place = place;
            return this;
        }

        public TableInputBuilder withHomeStats(Stats homeStats) {
            this.homeStats = homeStats;
            return this;
        }

        public TableInputBuilder withAwayStats(Stats awayStats) {
            this.awayStats = awayStats;
            return this;
        }

        public TableInput build() {
            return new TableInput(season, team, place, homeStats, awayStats);
        }

    }


}
