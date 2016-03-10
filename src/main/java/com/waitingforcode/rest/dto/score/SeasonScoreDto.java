package com.waitingforcode.rest.dto.score;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Map;

public class SeasonScoreDto {

    private int scoredHome;

    private int concededHome;

    private int scoredAway;

    private int concededAway;

    public SeasonScoreDto(@JsonProperty("scoredHome") int scoredHome, @JsonProperty("concededHome") int concededHome,
                          @JsonProperty("scoredAway") int scoredAway, @JsonProperty("concededAway") int concededAway) {
        this.scoredHome = scoredHome;
        this.concededHome = concededHome;
        this.scoredAway = scoredAway;
        this.concededAway = concededAway;
    }

    public int getScoredHome() {
        return scoredHome;
    }

    public int getConcededHome() {
        return concededHome;
    }

    public int getScoredAway() {
        return scoredAway;
    }

    public int getConcededAway() {
        return concededAway;
    }

    public static SeasonScoreDto valueOf(Map<String, Integer> goals) {
        return new SeasonScoreDto(goals.get("scored_home_games"), goals.get("conceded_home_games"),
                goals.get("scored_away_games"), goals.get("conceded_away_games"));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("scoredHome", scoredHome)
                .add("concededHome", concededHome)
                .add("scoredAway", scoredAway)
                .add("concededAway", concededAway).toString();
    }
}
