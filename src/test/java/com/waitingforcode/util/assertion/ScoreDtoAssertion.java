package com.waitingforcode.util.assertion;

import com.waitingforcode.rest.dto.score.ScoreDto;
import org.assertj.core.api.AbstractAssert;

public class ScoreDtoAssertion extends AbstractAssert<ScoreDtoAssertion, ScoreDto> {

    protected ScoreDtoAssertion(ScoreDto actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public static ScoreDtoAssertion assertThat(ScoreDto actual) {
        return new ScoreDtoAssertion(actual, ScoreDtoAssertion.class);
    }

    public ScoreDtoAssertion isFullyCorrect(String host, String guest, int round, int hostGoals, int guestGoals) {
        return isCorrectRound(round).hostGoalsAreCorrect(hostGoals).guestGoalsAreCorrect(guestGoals).isCorrectGuestTeam(guest)
                .isCorrectHostTeam(host);
    }

    public ScoreDtoAssertion isCorrectRound(int round) {
        if (actual.getRound() != round) {
            failWithMessage("Expected round is <%s> but was <%s>", round, actual.getRound());
        }
        return this;
    }

    public ScoreDtoAssertion guestGoalsAreCorrect(int guestGoals) {
        if (actual.getGuestGoals() != guestGoals) {
            failWithMessage("Expected guest goals is <%s> but was <%s>", guestGoals, actual.getGuestGoals());
        }
        return this;
    }

    public ScoreDtoAssertion hostGoalsAreCorrect(int hostGoals) {
        if (actual.getHostGoals() != hostGoals) {
            failWithMessage("Expected host goals is <%s> but was <%s>", hostGoals, actual.getHostGoals());
        }
        return this;
    }

    public ScoreDtoAssertion isCorrectHostTeam(String host) {
        if (!actual.getHostTeam().getName().equals(host)) {
            failWithMessage("Expected host team is <%s> but was <%s>", host, actual.getHostTeam());
        }
        return this;
    }

    public ScoreDtoAssertion isCorrectGuestTeam(String guest) {
        if (!actual.getGuestTeam().getName().equals(guest)) {
            failWithMessage("Expected guest team is <%s> but was <%s>", guest, actual.getGuestTeam());
        }
        return this;
    }

}
