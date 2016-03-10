package com.waitingforcode.rest.dto.table;


import com.google.common.base.MoreObjects;

import java.util.List;

public class TableDto {

    private String season;

    private List<PlaceDto> places;

    public TableDto(String season, List<PlaceDto> places) {
        this.season = season;
        this.places = places;
    }

    public String getSeason() {
        return this.season;
    }

    public List<PlaceDto> getPlaces() {
        return this.places;
    }

    public void addPlace(PlaceDto place) {
        places.add(place);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("season", season).add("places", places).toString();
    }

}
