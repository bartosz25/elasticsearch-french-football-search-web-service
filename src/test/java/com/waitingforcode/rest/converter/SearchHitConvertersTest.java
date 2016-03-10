package com.waitingforcode.rest.converter;

import com.google.common.collect.ImmutableMap;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.consumer.impl.TableConsumerImplTest;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreDto;
import com.waitingforcode.rest.dto.score.SeasonDto;
import com.waitingforcode.rest.dto.table.PlaceDto;
import com.waitingforcode.rest.dto.team.TeamDto;
import com.waitingforcode.util.assertion.PlaceStatsDtoAssertion;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SearchHitConvertersTest {

    @Test
    public void should_correctly_convert_to_team() {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = ImmutableMap.<String, Object>of("name", "Gazelec Ajaccio");
        when(hit.sourceAsMap()).thenReturn(data);

        TeamDto team = SearchHitConverters.toTeam().convert(hit);

        assertThat(team.getName()).isEqualTo("Gazelec Ajaccio");
    }

    @Test
    public void should_correctly_convert_to_score() {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = ImmutableMap.<String, Object>of("hostTeam", "Lille OSC", "guestTeam", "RC Lens",
                "round", 1, "hostGoals", 5, SearchDictionary.GUEST_GOALS, 2);
        when(hit.sourceAsMap()).thenReturn(data);

        ScoreDto score = SearchHitConverters.toScore().convert(hit);

        assertThat(score.getHostTeam().getName()).isEqualTo("Lille OSC");
        assertThat(score.getGuestTeam().getName()).isEqualTo("RC Lens");
        assertThat(score.getRound()).isEqualTo(1);
        assertThat(score.getHostGoals()).isEqualTo(5);
        assertThat(score.getGuestGoals()).isEqualTo(2);
    }

    @Test
    public void should_correctly_convert_to_season() {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = ImmutableMap.<String, Object>of("season", "2001/2002");
        when(hit.sourceAsMap()).thenReturn(data);

        SeasonDto seasonDto = SearchHitConverters.toSeason().convert(hit);

        assertThat(seasonDto.getSeason()).isEqualTo("2001/2002");
    }

    @Test
    public void should_correctly_convert_to_places_by_scored_goals() {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = ImmutableMap.<String, Object>of("season", "2001/2002", "place", 5);
        when(hit.sourceAsMap()).thenReturn(data);

        PlacesByScoredGoalsDto placesByScoredGoalsDto =
                SearchHitConverters.toPlacesByScoredGoals(hit, 15);

        assertThat(placesByScoredGoalsDto.getPlace()).isEqualTo(5);
        assertThat(placesByScoredGoalsDto.getSeason()).isEqualTo("2001/2002");
        assertThat(placesByScoredGoalsDto.getTimes()).isEqualTo(15);
    }

    @Test
    public void should_correctly_convert_to_places_by_season() {
        Map<String, Long> placesBySeason = ImmutableMap.of("2001/2002", 18L);
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = ImmutableMap.<String, Object>of("season", "2001/2002", "place", 5);
        when(hit.sourceAsMap()).thenReturn(data);

        PlacesByScoredGoalsDto placesByScoredGoalsDto =
                SearchHitConverters.toPlacesConverter(placesBySeason).apply(hit);

        assertThat(placesByScoredGoalsDto.getPlace()).isEqualTo(5);
        assertThat(placesByScoredGoalsDto.getSeason()).isEqualTo("2001/2002");
        assertThat(placesByScoredGoalsDto.getTimes()).isEqualTo(18);
    }

    @Test(expected = NullPointerException.class)
    public void should_correctly_convert_places_without_scoring_times() {
        Map<String, Long> placesBySeason = ImmutableMap.of("2002/2003", 18L);
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> data = ImmutableMap.<String, Object>of("season", "2001/2002", "place", 5);
        when(hit.sourceAsMap()).thenReturn(data);

        SearchHitConverters.toPlacesConverter(placesBySeason).apply(hit);
    }

    @Test
    public void should_correctly_convert_to_place() {
        SearchHit hit = mock(SearchHit.class);
        Map<String, Object> mapPlace1 = TableConsumerImplTest.getMap(1, "Montpellier HSC", "2001/2002", 90, 45,
                new Integer[]{5, 10, 15, 20, 25},
                new Integer[]{50, 55, 60, 65, 70});
        when(hit.sourceAsMap()).thenReturn(mapPlace1);

        PlaceDto place = SearchHitConverters.toPlace().convert(hit);

        assertThat(place.getGames()).isEqualTo(45);
        assertThat(place.getPoints()).isEqualTo(90);
        assertThat(place.getTeam()).isEqualTo("Montpellier HSC");
        assertThat(place.getSeason()).isEqualTo("2001/2002");
        PlaceStatsDtoAssertion.assertThat(place.getHomeStats()).assertMatchesStats(5, 10, 15).assertGoalsStats(20, 25);
        PlaceStatsDtoAssertion.assertThat(place.getAwayStats()).assertMatchesStats(50, 55, 60).assertGoalsStats(65, 70);
        PlaceStatsDtoAssertion.assertThat(place.getAllStats()).assertMatchesStats(55, 65, 75).assertGoalsStats(85, 95);
    }

}
