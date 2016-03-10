package com.waitingforcode.rest.dto.score;


import com.google.common.base.MoreObjects;
import com.waitingforcode.rest.dto.team.TeamDto;

public class ScoreDto {

    private TeamDto hostTeam;

    private TeamDto guestTeam;

    private int hostGoals;

    private int guestGoals;

    private int round;

    public TeamDto getHostTeam() {
        return hostTeam;
    }

    public void setHostTeam(TeamDto hostTeam) {
        this.hostTeam = hostTeam;
    }

    public TeamDto getGuestTeam() {
        return guestTeam;
    }

    public void setGuestTeam(TeamDto guestTeam) {
        this.guestTeam = guestTeam;
    }

    public int getHostGoals() {
        return hostGoals;
    }

    public void setHostGoals(int hostGoals) {
        this.hostGoals = hostGoals;
    }

    public int getGuestGoals() {
        return guestGoals;
    }

    public void setGuestGoals(int guestGoals) {
        this.guestGoals = guestGoals;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hostTeam", hostTeam).add("guestTeam", guestTeam)
                .add("hostGoals", hostGoals).add("guestGoals", guestGoals).add("round", round)
                .toString();
    }

}
