package com.waitingforcode.rest.dto.score;


import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatsDto {

    private List<ScoreStatsDto> games = new ArrayList<>();

    private Map<String, SeasonScoreDto> goals = new TreeMap<>();

    public List<ScoreStatsDto> getGames() {
        return games;
    }

    public void addGame(ScoreStatsDto...matches) {
        for (ScoreStatsDto match : matches) {
            games.add(match);
        }
    }

    public Map<String, SeasonScoreDto> getGoals() {
        return goals;
    }

    public void addGoal(String season, SeasonScoreDto scoreDto) {
        goals.put(season, scoreDto);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("games", games).add("goals", goals).toString();
    }

}
