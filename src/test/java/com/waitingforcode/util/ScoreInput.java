package com.waitingforcode.util;


public class ScoreInput {

    private String season;
    private String hostTeam;
    private String guestTeam;
    private Integer hostGoals;
    private Integer guestGoals;
    private Integer allGoals;
    private Integer round;

    public ScoreInput(String season, Integer round, String hostTeam, String guestTeam, Integer hostGoals, Integer guestGoals) {
        this.season = season;
        this.round = round;
        this.hostTeam = hostTeam;
        this.guestTeam = guestTeam;
        this.hostGoals = hostGoals;
        this.guestGoals = guestGoals;
        this.allGoals = hostGoals + guestGoals;
    }

    public String getSeason() {
        return season;
    }

    public String getHostTeam() {
        return hostTeam;
    }

    public String getGuestTeam() {
        return guestTeam;
    }

    public Integer getHostGoals() {
        return hostGoals;
    }

    public Integer getGuestGoals() {
        return guestGoals;
    }

    public Integer getAllGoals() {
        return allGoals;
    }

    public Integer getRound() {
        return round;
    }

    public static class Builder {
        private String season;
        private String hostTeam;
        private String guestTeam;
        private Integer hostGoals;
        private Integer guestGoals;
        private Integer round;

        public Builder forSeason(String season) {
            this.season = season;
            return this;
        }

        public Builder withTeams(String hostTeam, String guestTeam) {
            this.hostTeam = hostTeam;
            this.guestTeam = guestTeam;
            return this;
        }

        public Builder withGoals(Integer hostGoals, Integer guestGoals) {
            this.hostGoals = hostGoals;
            this.guestGoals = guestGoals;
            return this;
        }

        public Builder playedInRound(Integer round) {
            this.round = round;
            return this;
        }

        public ScoreInput build() {
            return new ScoreInput(season, round, hostTeam, guestTeam, hostGoals, guestGoals);
        }

    }

}
