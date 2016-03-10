package com.waitingforcode.rest.dto.team;

import com.google.common.base.MoreObjects;

public class TeamDto {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TeamDto valueOf(String name) {
        TeamDto team = new TeamDto();
        team.setName(name);
        return team;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).toString();
    }
}
