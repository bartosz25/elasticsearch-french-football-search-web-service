package com.waitingforcode.rest.dto.table;


import com.google.common.base.MoreObjects;

public class PlaceStatsDto {

    private int wins;

    private int draws;

    private int losses;

    private int goalsScored;

    private int goalsConceded;


    public static enum Types {
        ALL, HOME, AWAY;
    }

    private PlaceStatsDto(int wins, int draws, int losses, int goalsScored, int goalsConceded) {
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsScored = goalsScored;
        this.goalsConceded = goalsConceded;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getGoalsScored() {
        return goalsScored;
    }

    public int getGoalsConceded() {
        return goalsConceded;
    }

    public static class Builder {
        private int wins;
        private int draws;
        private int losses;
        private int goalsScored;
        private int goalsConceded;

        public Builder withWinsDrawsAndLosses(int wins, int draws, int losses) {
            this.wins = wins;
            this.draws = draws;
            this.losses = losses;
            return this;
        }

        public Builder forScoredAndConcededGoals(int goalsScored, int goalsConceded) {
            this.goalsScored = goalsScored;
            this.goalsConceded = goalsConceded;
            return this;
        }

        public PlaceStatsDto build() {
            return new PlaceStatsDto(wins, draws, losses, goalsScored, goalsConceded);
        }

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("wins", wins).add("draws", draws).add("losses", losses)
                .add("goalsScored", goalsScored).add("goalsConceded", goalsConceded).toString();
    }

}
