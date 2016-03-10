package com.waitingforcode.rest.dto.score;


import com.google.common.base.MoreObjects;

public class BaseScoreDto {

    private String score;

    private int games;

    public BaseScoreDto(String score, int games) {
        this.score = score;
        this.games = games;
    }

    public String getScore() {
        return score;
    }

    public int getGames() {
        return games;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("score", score).add("games", games)
                .toString();
    }
}
