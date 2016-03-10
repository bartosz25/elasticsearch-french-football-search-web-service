package com.waitingforcode.elasticsearch.comparator;

import com.google.common.collect.Lists;
import com.waitingforcode.rest.dto.score.ScoreDto;
import com.waitingforcode.rest.dto.team.TeamDto;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreComparatorTest {

    private static final String RC_LENS = "RC Lens";
    private static final String OM = "Olympique Marseille";
    private static final String OL = "Olympique Lyon";
    private static final String AC_AJACCIO = "AC Ajaccio";

    @Test
    public void should_correctly_sort_scores_by_round_and_host_teams() {
        ScoreDto match1Round1 = score(RC_LENS, OM, 1, 3, 0);
        ScoreDto match2Round1 = score(OL, AC_AJACCIO, 1, 2, 2);
        ScoreDto match1Round2 = score(RC_LENS, OL, 2, 0, 0);
        ScoreDto match2Round2 = score(AC_AJACCIO, OL, 2, 1, 0);
        ScoreDto match1Round3 = score(AC_AJACCIO, OM, 3, 1, 0);
        ScoreDto match2Round3 = score(OL, RC_LENS, 3, 3, 3);
        List<ScoreDto> scores = Lists.newArrayList(match1Round1, match2Round1, match1Round2, match2Round2, match1Round3, match2Round3);

        Collections.sort(scores, ScoreComparators.GLOBAL);

        assertThat(scores).extracting("hostTeam").extracting("name").containsExactly("Olympique Lyon", "RC Lens", "AC Ajaccio", "RC Lens", "AC Ajaccio", "Olympique Lyon");
        assertThat(scores).extracting("round").containsExactly(1, 1, 2, 2, 3, 3);
    }

    private ScoreDto score(String hostTeam, String guestTeam, int round, int hostGoals, int guestGoals) {
        ScoreDto score = new ScoreDto();
        score.setHostTeam(TeamDto.valueOf(hostTeam));
        score.setGuestTeam(TeamDto.valueOf(guestTeam));
        score.setRound(round);
        score.setHostGoals(hostGoals);
        score.setGuestGoals(guestGoals);
        return score;
    }

}
