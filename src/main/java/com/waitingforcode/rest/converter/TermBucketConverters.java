package com.waitingforcode.rest.converter;


import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;

import java.util.Map;
import java.util.function.Function;

public final class TermBucketConverters {

    private TermBucketConverters() {
        throw new ConstructorNotInvokableException();
    }

    public static Function<Terms.Bucket, AdversaryDto> toAdversary() {
        return ToAdversaryConverter.INSTANCE;
    }

    public static Function<Terms.Bucket, HockeyScoreDto> toHockeyScore() {
        return ToHockeyScoreConverter.INSTANCE;
    }

    private enum ToHockeyScoreConverter implements Function<Terms.Bucket, HockeyScoreDto> {
        INSTANCE {
            @Override
            public HockeyScoreDto apply(Terms.Bucket roundBucket) {
                return new HockeyScoreDto((long) roundBucket.getKeyAsNumber(), roundBucket.getDocCount());
            }
        }
    }

    private enum ToAdversaryConverter implements Function<Terms.Bucket, AdversaryDto> {
        INSTANCE {
            @Override
            public AdversaryDto apply(Terms.Bucket bucket) {
                Map<String, Aggregation> aggs = bucket.getAggregations().asMap();

                InternalFilter filterHost = (InternalFilter) aggs.get(SearchDictionary.HOME_ADVERSARY_GOALS);
                InternalSum hostGoals = filterHost.getAggregations().get(SearchDictionary.GOALS);
                InternalFilter filterGuest = (InternalFilter) aggs.get(SearchDictionary.AWAY_ADVERSARY_GOALS);
                InternalSum guestGoals = filterGuest.getAggregations().get(SearchDictionary.GOALS);

                return new AdversaryDto(bucket.getKeyAsString(), (int) hostGoals.getValue(), (int) guestGoals.getValue());
            }
        };
    }
}
