package com.waitingforcode.rest.resources;

import com.waitingforcode.elasticsearch.consumer.TableConsumer;
import com.waitingforcode.elasticsearch.service.TableService;
import com.waitingforcode.rest.dto.general.CollectionResponseDto;
import com.waitingforcode.rest.dto.table.PlaceDto;
import com.waitingforcode.rest.dto.table.TableDto;
import com.waitingforcode.rest.http.params.SeasonPathParam;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Path("seasons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SeasonsResource {

    @Autowired
    private TableService tableService;

    @Autowired
    private TableConsumer tableConsumer;

    @GET
    @Path("{season}/table")
    public TableDto getTableBySeason(@PathParam("season") SeasonPathParam season) {
        SearchResponse tableResponse = tableService.findTableBySeason(season.getQueryForm());
        return tableConsumer.getTableForSeason(tableResponse, season.getQueryForm());
    }

    @GET
    @Path("teams/{team}/places")
    public CollectionResponseDto<PlaceDto> getPlacesByTeam(@PathParam("team") String team) {
        SearchResponse searchResponse = tableService.findPlacesByTeam(team);
        CollectionResponseDto<PlaceDto> table = new CollectionResponseDto<>();
        table.setItems(tableConsumer.getPlacesByTeamForEachSeason(searchResponse));
        return table;
    }

}