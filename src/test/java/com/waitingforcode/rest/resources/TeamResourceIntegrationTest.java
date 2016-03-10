package com.waitingforcode.rest.resources;

import com.google.common.collect.Lists;
import com.waitingforcode.configuration.ServletContainerConfigurationRunner;
import com.waitingforcode.rest.dto.general.CollectionResponseDto;
import com.waitingforcode.util.Indexers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ServletContainerConfigurationRunner.class})
@WebIntegrationTest
@ActiveProfiles( profiles = {"test"})
public class TeamResourceIntegrationTest {

    @Autowired
    private Indexers indexers;

    private RestTemplate restTemplate = new TestRestTemplate();

    @After
    public void reset() {
        indexers.cleanAll("teams");
    }

    @Test
    public void should_find_all_teams() throws URISyntaxException {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia"));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/teams"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.hasBody()).isTrue();
        assertThat(response.getBody().getAllElements()).isEqualTo(3L);
        assertThat(response.getBody().getItems()).extracting("name").containsOnly("AC Ajaccio", "AS Monaco", "SC Bastia");
    }

    @Test
    public void should_correctly_paginate_over_all_teams() throws URISyntaxException {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia", "rc_lens"));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/teams?page=3&perPage=1"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.hasBody()).isTrue();
        assertThat(response.getBody().getAllElements()).isEqualTo(4L);
        assertThat(response.getBody().getItems()).extracting("name").containsOnly("RC Lens");
    }

    @Test
    public void should_correctly_find_team_by_name_for_normal_search() throws URISyntaxException {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia"));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/teams/team?name=as%20monaco"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.hasBody()).isTrue();
        assertThat(response.getBody().getItems()).hasSize(1);
        assertThat(response.getBody().getAllElements()).isEqualTo(1L);
        List<Map<String, String>> teams = response.getBody().getItems();
        assertThat(teams.get(0).get("name")).isEqualTo("AS Monaco");
    }

    @Test
    public void should_find_team_with_fuzzy_search() throws URISyntaxException {
        indexers.teamsFromFile(Lists.newArrayList("ac_ajaccio", "as_monaco", "sc_bastia"));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CollectionResponseDto> response =
                restTemplate.getForEntity(new URI("http://localhost:8888/rest/teams/team?name=sc%20bstia&type=FUZZY"), CollectionResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.hasBody()).isTrue();
        assertThat(response.getBody().getItems()).hasSize(1);
        assertThat(response.getBody().getAllElements()).isEqualTo(1L);
        List<Map<String, String>> teams = response.getBody().getItems();
        assertThat(teams.get(0).get("name")).isEqualTo("SC Bastia");
    }

}
