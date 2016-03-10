package com.waitingforcode.elasticsearch.data;


public enum IndexTypes {

    TEAMS("teams"), SCORES("scores"), TABLES("tables");

    private String type;

    private IndexTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
