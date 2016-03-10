package com.waitingforcode.rest.sort;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class AdversaryDtoSortingTest {

    @DataProvider
    public static Object[][] scoredGoals() {
        return new Object[][] {
                {2, 2, 1, 1, -1},
                {1, 1, 2, 2, 1},
                {2, 0, 1, 1, 0}
        };
    }

    @Test
    @UseDataProvider("scoredGoals")
    public void should_detect_that_teams_scored_the_same_number_of_goals(int team1HostGoals, int team1GuestGoals,
                                                                         int team2HostGoals, int team2GuestGoals,
                                                                         int expected) {
        assertThat(AdversaryDtoSorting.SCORED_GOALS_DESC.compare(new AdversaryDto("", team1HostGoals, team1GuestGoals),
                new AdversaryDto("", team2HostGoals, team2GuestGoals))).isEqualTo(expected);
    }

}
