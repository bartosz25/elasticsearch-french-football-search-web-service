package com.waitingforcode.rest.dto.score;


import com.google.common.base.MoreObjects;

public class PlacesByScoredGoalsDto {

    private String season;

    private int place;

    private int times;

    public PlacesByScoredGoalsDto(String season, int place, int times) {
        this.season = season;
        this.place = place;
        this.times = times;
    }


    public String getSeason() {
        return season;
    }

    public int getPlace() {
        return place;
    }

    public int getTimes() {
        return times;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("season", season).add("place", place).add("times", times)
                .toString();
    }

}
