package com.waitingforcode.rest.dto.score;


import com.google.common.base.MoreObjects;

public class HockeyScoreDto {

    private long round;

    private long games;

    public HockeyScoreDto(long round, long games) {
        this.round = round;
        this.games = games;
    }

    public long getRound() {
        return round;
    }

    public long getGames() {
        return games;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("round", round).add("games", games).toString();
    }

}
