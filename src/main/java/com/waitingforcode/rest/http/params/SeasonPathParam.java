package com.waitingforcode.rest.http.params;

import com.google.common.base.MoreObjects;

public class SeasonPathParam implements SearchableParam<String> {

    private static final String SEPARATOR = "/";

    private int startYear;

    private int endYear;

    // Constructor with single String parameter is one of solutions to make some of @PathParam being another objects than primitives
    // https://jersey.java.net/apidocs/2.11/jersey/javax/ws/rs/PathParam.html
    public SeasonPathParam(String seasonNoSeparator) {
        String[] parts = seasonNoSeparator.split("_");
        this.startYear = Integer.valueOf(parts[0]);
        this.endYear = Integer.valueOf(parts[1]);
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    @Override
    public String getQueryForm() {
        return startYear+SEPARATOR+endYear;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("startYear", startYear).add("endYear", endYear)
                .toString();
    }

}
