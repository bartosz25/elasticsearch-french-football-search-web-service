package com.waitingforcode.rest.resources;

import com.waitingforcode.elasticsearch.consumer.TeamConsumer;
import com.waitingforcode.elasticsearch.query.CountableSearchResponse;
import com.waitingforcode.elasticsearch.query.QueryingModes;
import com.waitingforcode.elasticsearch.service.TeamService;
import com.waitingforcode.elasticsearch.util.Pagination;
import com.waitingforcode.rest.dto.general.CollectionResponseDto;
import com.waitingforcode.rest.dto.team.TeamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Component
@Path("/teams")
@Produces(MediaType.APPLICATION_JSON)
public class TeamsResource {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamConsumer teamConsumer;

    @GET
    public CollectionResponseDto<TeamDto> getAllTeams(@BeanParam Pagination pagination) {
        CountableSearchResponse esResponse = teamService.findAllTeams(pagination);
        CollectionResponseDto<TeamDto> response = new CollectionResponseDto<>();
        response.setAllElements(esResponse.getAllElements());
        response.setItems(teamConsumer.getTeamsFromResponse(esResponse.getDataResponse()));
        return response;
    }

    @GET
    @Path("team")
    public CollectionResponseDto<TeamDto> getAllTeamsMatchingName(@QueryParam("name") String name,
                                                                  @BeanParam Pagination pagination,
                                                               @DefaultValue("NORMAL") @QueryParam("type") String type) {
        CountableSearchResponse esResponse;
        QueryingModes queryingMode = QueryingModes.valueOf(type);
        if (queryingMode == QueryingModes.FUZZY) {
            esResponse = teamService.findAllTeamsByNameAndFuzziness(name, pagination);
        } else {
            esResponse = teamService.findAllTeamsByName(name, pagination);
        }

        CollectionResponseDto<TeamDto> response = new CollectionResponseDto<>();
        response.setAllElements(esResponse.getAllElements());
        response.setItems(teamConsumer.getTeamsFromResponse(esResponse.getDataResponse()));
        return response;
    }

}
