package com.waitingforcode.elasticsearch.service.impl;


import com.google.common.collect.Lists;
import com.waitingforcode.configuration.IntegrationTestConfiguration;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.service.TeamService;
import com.waitingforcode.elasticsearch.util.Pagination;
import com.waitingforcode.util.Indexers;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@ActiveProfiles( profiles = {"test"})
public class TeamServiceImplIntegrationTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private Indexers indexers;

    private Pagination pagination = new Pagination();

    @After
    public void reset() {
        indexers.cleanAll("teams");
    }

    @Test
    public void should_find_all_indexed_teams() throws InterruptedException {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia"));

        CountableSearchResponse response = teamService.findAllTeams(pagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits().length).isEqualTo(3);
        assertThat(response.getAllElements()).isEqualTo(3L);
    }

    @Test
    public void should_find_all_indexed_teams_and_paginate_them() throws InterruptedException {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "sc_bastia", "rc_lens", "as_monaco", "lille_osc", "paris_sg"));
        Pagination smallPagination = new Pagination();
        smallPagination.setPerPage(2);

        CountableSearchResponse response = teamService.findAllTeams(smallPagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits().length).isEqualTo(2);
        assertThat(hits.getAt(0).sourceAsMap().get("name")).isEqualTo("AC Ajaccio");
        assertThat(hits.getAt(1).sourceAsMap().get("name")).isEqualTo("AS Monaco");
        assertThat(response.getAllElements()).isEqualTo(6L);
    }

    @Test
    public void should_find_ajaccio_team() {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia"));

        CountableSearchResponse response = teamService.findAllTeamsByName("ac ajaccio", pagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits().length).isEqualTo(1);
        assertThat(hits.getAt(0).sourceAsMap().get("name")).isEqualTo("AC Ajaccio");
        assertThat(response.getAllElements()).isEqualTo(1L);
    }

    @Test
    public void should_find_two_olympique_teams() {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia", "olympique_de_marseille",
                "olympique_lyonnais"));
        final List<String> names = new ArrayList<>();

        CountableSearchResponse response = teamService.findAllTeamsByName("Olympique Lyonnais", pagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits().length).isEqualTo(2);
        hits.forEach((SearchHit hit) -> {
            names.add(""+hit.sourceAsMap().get("name"));
        });
        assertThat(names).containsOnly("Olympique Lyonnais", "Olympique de Marseille");
        assertThat(response.getAllElements()).isEqualTo(2L);
    }

    @Test
    public void should_find_sc_bastia_by_sc_synonym_analyzer() {
        indexers.teamsFromFile(Lists.newArrayList("sc_bastia"));

        CountableSearchResponse response = teamService.findAllTeamsByName("sporting club bastia", pagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits().length).isEqualTo(1);
        assertThat(hits.getAt(0).sourceAsMap().get("name")).isEqualTo("SC Bastia");
        assertThat(response.getAllElements()).isEqualTo(1L);
    }

    @Test
    public void should_find_team_by_fuzzy_search() {
        indexers.teamsFromFile(Lists.newArrayList("olympique_lyonnais", "as_monaco", "olympique_de_marseille"));

        CountableSearchResponse response = teamService.findAllTeamsByNameAndFuzziness("olympique de marseile", pagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits().length).isEqualTo(1);
        assertThat(hits.getAt(0).sourceAsMap().get("name")).isEqualTo("Olympique de Marseille");
        assertThat(response.getAllElements()).isEqualTo(1L);
    }

    @Test
    public void should_not_find_team_by_too_big_fuzzy_distance() {
        indexers.teamsFromFile(Lists.newArrayList("olympique_lyonnais", "as_monaco", "olympique_de_marseille"));

        CountableSearchResponse response = teamService.findAllTeamsByNameAndFuzziness("maseile", pagination);

        SearchHits hits = response.getDataResponse().getHits();
        assertThat(hits.getHits()).isEmpty();
        assertThat(response.getAllElements()).isEqualTo(0L);
    }

}
