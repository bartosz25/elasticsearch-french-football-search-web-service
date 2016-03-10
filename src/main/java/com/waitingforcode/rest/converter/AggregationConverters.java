package com.waitingforcode.rest.converter;


import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AggregationConverters {

    private AggregationConverters() {
        throw new ConstructorNotInvokableException();
    }

    public static Function<Aggregation, List<HockeyScoreDto>> toHockeyLists() {
        return ToHockeyListsConverter.INSTANCE;
    }

    private enum ToHockeyListsConverter implements Function<Aggregation, List<HockeyScoreDto>> {
        INSTANCE {
            @Override
            public List<HockeyScoreDto> apply(Aggregation aggregation) {
                return ((LongTerms) aggregation).getBuckets()
                        .stream()
                        .map(TermBucketConverters.toHockeyScore())
                        .collect(Collectors.toList());
            }
        }
    }

}
