package com.waitingforcode.rest.dto.score;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.waitingforcode.elasticsearch.data.ScoreTypes;

import java.util.ArrayList;
import java.util.List;

public class ScoreStatsDto {

    private ScoreTypes type;

    private List<Match> scores = new ArrayList<>();

    public ScoreStatsDto(@JsonProperty("type") ScoreTypes type) {
        this.type = type;
    }

    public ScoreTypes getType() {
        return type;
    }

    public List<Match> getScores() {
        return scores;
    }

    public void addScores(Match...matches) {
        for (Match match : matches) {
            scores.add(match);
        }
    }

    public static class Match {
        private int goals;
        private int games;

        public Match(@JsonProperty("goals") int goals, @JsonProperty("games") int games) {
            this.goals = goals;
            this.games = games;
        }

        public int getGoals() {
            return goals;
        }

        public int getGames() {
            return games;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("goals", goals).add("games", games).toString();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("scores", scores).toString();
    }

}
