package com.waitingforcode.util.assertion;

import com.waitingforcode.rest.dto.table.PlaceStatsDto;
import org.assertj.core.api.AbstractAssert;

public class PlaceStatsDtoAssertion extends AbstractAssert<PlaceStatsDtoAssertion, PlaceStatsDto> {

    protected PlaceStatsDtoAssertion(PlaceStatsDto actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public static PlaceStatsDtoAssertion assertThat(PlaceStatsDto actual) {
        return new PlaceStatsDtoAssertion(actual, PlaceStatsDtoAssertion.class);
    }

    public PlaceStatsDtoAssertion assertMatchesStats(int wins, int draws, int losses) {
        return checkWins(wins).checkDraws(draws).checkLosses(losses);
    }

    public PlaceStatsDtoAssertion assertGoalsStats(int scored, int conceded) {
        return checkScoredGoals(scored).checkConcededGoals(conceded);
    }

    public PlaceStatsDtoAssertion checkScoredGoals(int scored) {
        if (actual.getGoalsScored() != scored) {
            failWithMessage("Expected scored is <%s> but was <%s>", scored, actual.getGoalsScored());
        }
        return this;
    }

    public PlaceStatsDtoAssertion checkConcededGoals(int conceded) {
        if (actual.getGoalsConceded() != conceded) {
            failWithMessage("Expected conceded is <%s> but was <%s>", conceded, actual.getGoalsConceded());
        }
        return this;
    }

    public PlaceStatsDtoAssertion checkWins(int wins) {
        if (actual.getWins() != wins) {
            failWithMessage("Expected wins is <%s> but was <%s>", wins, actual.getWins());
        }
        return this;
    }

    public PlaceStatsDtoAssertion checkDraws(int draws) {
        if (actual.getDraws() != draws) {
            failWithMessage("Expected draws is <%s> but was <%s>", draws, actual.getDraws());

        }
        return this;
    }

    public PlaceStatsDtoAssertion checkLosses(int losses) {
        if (actual.getLosses() != losses) {
            failWithMessage("Expected losses is <%s> but was <%s>", losses, actual.getLosses());
        }
        return this;
    }

}
