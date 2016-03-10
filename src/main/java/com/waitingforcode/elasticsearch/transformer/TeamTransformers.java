package com.waitingforcode.elasticsearch.transformer;

import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import com.waitingforcode.rest.converter.SearchHitConverters;
import com.waitingforcode.rest.dto.team.TeamDto;
import org.elasticsearch.search.SearchHit;

import java.util.function.Function;

public final class TeamTransformers {

    private TeamTransformers() {
        throw new ConstructorNotInvokableException();
    }

    public static Function<SearchHit, TeamDto>  fromSearchHit() {
        return SearchHitMappers.BASIC;
    }


    private enum SearchHitMappers implements Function<SearchHit, TeamDto> {

        BASIC {
            @Override
            public TeamDto apply(SearchHit searchHit) {
                return SearchHitConverters.toTeam().convert(searchHit);
            }
        }
    }
}
