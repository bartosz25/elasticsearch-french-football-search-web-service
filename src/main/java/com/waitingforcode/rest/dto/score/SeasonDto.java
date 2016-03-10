package com.waitingforcode.rest.dto.score;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeasonDto {

    private String season;

    private List<ScoreDto> scores = new ArrayList<ScoreDto>();

    public void addScore(ScoreDto...scoresArgs) {
        for (ScoreDto score : scoresArgs) {
            scores.add(score);
        }
    }

    public List<ScoreDto> getScores() {
        return scores;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public static SeasonDto valueOf(String season) {
        SeasonDto seasonDto = new SeasonDto();
        seasonDto.setSeason(season);
        return seasonDto;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSeason());
    }

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof SeasonDto)) {
            return false;
        }
        SeasonDto seasonDto = (SeasonDto) another;
        return Objects.equals(getSeason(), seasonDto.getSeason());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("scores", scores).toString();
    }
}
