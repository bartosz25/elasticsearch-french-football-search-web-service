package com.waitingforcode.elasticsearch.transformer;

import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import com.waitingforcode.rest.converter.AggregationConverters;
import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScoreTransformers {

    private ScoreTransformers() {
        throw new ConstructorNotInvokableException();
    }

    public static Function<Aggregation, List<BaseScoreDto>> aggregatorToBaseScore() {
        return AggregationToBaseScoresConverter.INSTANCE;
    }

    public static Function<Terms.Bucket, List<BaseScoreDto>> bucketToScores() {
        return BucketToBaseScores.INSTANCE;
    }

    public static Function<Terms.Bucket, List<HockeyScoreDto>> bucketToHockeyScores() {
        return BucketToHockeyScores.INSTANCE;
    }

    private enum AggregationToBaseScoresConverter implements Function<Aggregation, List<BaseScoreDto>> {
        INSTANCE;

        @Override
        public List<BaseScoreDto> apply(Aggregation aggregation) {
            StringTerms seasonBucket = (StringTerms) aggregation;
            return seasonBucket.getBuckets().stream()
                    .map(roundBucket -> new BaseScoreDto(roundBucket.getKeyAsString(), (int) roundBucket.getDocCount()))
                    .collect(Collectors.toList());
        }
    }

    private enum BucketToBaseScores implements Function<Terms.Bucket, List<BaseScoreDto>> {
        INSTANCE;

        @Override
        public List<BaseScoreDto> apply(Terms.Bucket bucket) {
            return bucket.getAggregations().asList().stream()
                    .map(ScoreTransformers.aggregatorToBaseScore())
                    .findFirst()
                    .get();
        }
    }

    private enum BucketToHockeyScores implements Function<Terms.Bucket, List<HockeyScoreDto>> {
        INSTANCE;

        @Override
        public List<HockeyScoreDto> apply(Terms.Bucket bucket) {
            return bucket.getAggregations().asList().stream()
                    .map(AggregationConverters.toHockeyLists())
                    .findFirst()
                    .get();
        }
    }

}
