package com.waitingforcode.rest.dto.score;


import com.google.common.base.MoreObjects;

public class AdversaryDto {

    private String team;

    private int goals;

    private int scoredAsHost;

    private int scoredAsGuest;

    public AdversaryDto(String team, int scoredAsHost, int scoredAsGuest) {
        this.team = team;
        this.goals = scoredAsHost + scoredAsGuest;
        this.scoredAsHost = scoredAsHost;
        this.scoredAsGuest = scoredAsGuest;
    }

    public String getTeam() {
        return team;
    }

    public int getGoals() {
        return goals;
    }

    public int getScoredAsHost() {
        return scoredAsHost;
    }

    public int getScoredAsGuest() {
        return scoredAsGuest;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("team", team).add("goals", goals).add("scoredAsHost", scoredAsHost).add("scoredAsGuest", scoredAsGuest)
                .toString();
    }

}
