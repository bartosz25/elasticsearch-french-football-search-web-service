package com.waitingforcode.elasticsearch.transformer;

import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import com.waitingforcode.rest.converter.SearchHitConverters;
import com.waitingforcode.rest.dto.score.SeasonDto;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class SeasonTransformers {

    private SeasonTransformers() {
        throw new ConstructorNotInvokableException();
    }

    public static Function<List<SearchHit>, List<SeasonDto>> fromSearchHitsToSeasons() {
        return new SearchHitsToSeasons();
    }

    private static class SearchHitsToSeasons implements Function<List<SearchHit>, List<SeasonDto>> {

        private List<SeasonDto> seasons = new ArrayList<>();

        @Override
        public List<SeasonDto> apply(List<SearchHit> hits) {
            for (SearchHit searchHit : hits) {
                SeasonDto season = SearchHitConverters.toSeason().convert(searchHit);
                int index = seasons.indexOf(season);
                if (index > -1) {
                    season = seasons.get(index);
                } else {
                    seasons.add(season);
                }
                season.addScore(SearchHitConverters.toScore().convert(searchHit));
            }
            return seasons;
        }
    }

}
