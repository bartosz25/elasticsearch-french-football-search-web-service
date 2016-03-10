package com.waitingforcode.elasticsearch.service.impl;


import com.google.common.collect.Lists;
import com.waitingforcode.configuration.IntegrationTestConfiguration;
import com.waitingforcode.elasticsearch.service.TableService;
import com.waitingforcode.util.Indexers;
import com.waitingforcode.util.TableInput;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@ActiveProfiles( profiles = {"test"})
public class TableServiceImplIntegrationTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private Indexers indexers;

    private String asMonaco = "AS Monaco";

    private String checkKey0102Asm
            = "2001/2002:AS Monaco:3:89:40|89p:40g:25w:13d:2l:109s:25c|50p:20g:15w:4d:1l:59s:10c|40p:20g:10w:9d:1l:50s:15c";
    private String checkKey0203Asm
            = "2002/2003:AS Monaco:5:81:39|81p:39g:23w:11d:5l:130s:20c|56p:19g:18w:1d:0l:80s:5c|26p:20g:5w:10d:5l:50s:15c";
    private String checkKey0203Rcl
            = "2002/2003:RC Lens:15:54:40|54p:40g:14w:11d:15l:70s:65c|38p:20g:12w:1d:7l:55s:25c|17p:20g:2w:10d:8l:15s:40c";

    @Before
    public void init() {
        TableInput.TableInputBuilder builder = new TableInput.TableInputBuilder();
        TableInput season0102 = builder.forTeam(asMonaco).forSeason("2001/2002").andPlace(3)
                .withHomeStats(new TableInput.StatsBuilder().games(15, 4, 1).goals(59, 10).build())
                .withAwayStats(new TableInput.StatsBuilder().games(10, 9, 1).goals(50, 15).build())
                .build();
        TableInput season0203 = builder.forSeason("2002/2003").andPlace(5)
                .withHomeStats(new TableInput.StatsBuilder().games(18, 1, 0).goals(80, 5).build())
                .withAwayStats(new TableInput.StatsBuilder().games(5, 10, 5).goals(50, 15).build())
                .build();
        TableInput season0304 = builder.forTeam("AC Ajaccio").forSeason("2003/2004").andPlace(15)
                .withHomeStats(new TableInput.StatsBuilder().games(12, 1, 7).goals(55, 25).build())
                .withAwayStats(new TableInput.StatsBuilder().games(2, 10, 8).goals(15, 40).build())
                .build();
        TableInput season0203Lens = builder.forTeam("RC Lens").forSeason("2002/2003").build();
        indexers.tablesFromObjects(Lists.newArrayList(season0102, season0203, season0203Lens, season0304));
    }

    @After
    public void reset() {
        indexers.cleanAll("tables");
    }

    @Test
    public void should_find_as_monaco_places_in_2_of_3_expected_seasons() {
        SearchResponse monacoPlaces = tableService.findTeamPlaceInSeasons(asMonaco.toLowerCase(),
                Lists.newArrayList("2001/2002", "2002/2003", "2003/2004"));

        assertThat(monacoPlaces.getHits()).hasSize(2);
        List<String> seasonLines = Stream.of(monacoPlaces.getHits().getHits()).map(new ToSeasonLine()).collect(Collectors.toList());
        assertThat(seasonLines).containsOnly(checkKey0102Asm, checkKey0203Asm);
    }

    @Test
    public void should_not_find_places_for_not_indexed_team_in_expected_seasons() {
        SearchResponse lillePlaces = tableService.findTeamPlaceInSeasons("Lille OSC".toLowerCase(),
                Lists.newArrayList("2001/2002", "2002/2003", "2003/2004"));

        assertThat(lillePlaces.getHits()).isEmpty();
    }

    @Test
    public void should_find_correct_team_places_in_2_from_4_seasons() throws InterruptedException {
        SearchResponse monacoPlaces = tableService.findPlacesByTeam(asMonaco.toLowerCase());

        assertThat(monacoPlaces.getHits()).hasSize(2);
        List<String> seasonLines = Stream.of(monacoPlaces.getHits().getHits()).map(new ToSeasonLine()).collect(Collectors.toList());
        assertThat(seasonLines).containsOnly(checkKey0102Asm, checkKey0203Asm);
    }

    @Test
    public void should_find_existing_table_for_given_season() {
        SearchResponse result = tableService.findTableBySeason("2002/2003");

        assertThat(result.getHits()).hasSize(2);
        List<String> seasonLines = Stream.of(result.getHits().getHits()).map(new ToSeasonLine()).collect(Collectors.toList());
        assertThat(seasonLines).containsOnly(checkKey0203Asm, checkKey0203Rcl);
    }

    @Test
    public void should_not_find_table_for_not_indexed_season() {
        SearchResponse result = tableService.findTableBySeason("1990/1991");

        assertThat(result.getHits()).isEmpty();
    }

    private static class ToSeasonLine implements Function<SearchHit, String> {

        @Override
        public String apply(SearchHit searchHit) {
            Map<String, Object> data = searchHit.sourceAsMap();
            Map<String, Object> allStats = (Map<String, Object>) data.get("allStats");
            Map<String, Object> homeStats = (Map<String, Object>) data.get("homeStats");
            Map<String, Object> awayStats = (Map<String, Object>) data.get("awayStats");

            StringBuilder result = new StringBuilder(""+data.get("season")).append(":").append(""+data.get("team"))
                    .append(":").append(data.get("place")).append(":").append(data.get("points")).append(":").append(data.get("games"))
                    .append("|").append(statsToLine(allStats))
                    .append("|").append(statsToLine(homeStats))
                    .append("|").append(statsToLine(awayStats));
            return result.toString();
        }

        private String statsToLine(Map<String, Object> stats) {
            return new StringBuilder(""+stats.get("points")).append("p:")
                    .append(stats.get("games")).append("g:")
                    .append(stats.get("wins")).append("w:")
                    .append(stats.get("draws")).append("d:")
                    .append(stats.get("losses")).append("l:")
                    .append(stats.get("scored")).append("s:")
                    .append(stats.get("conceded")).append("c")
                    .toString();
        }
    }

}
