package com.waitingforcode.rest.converter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AggregationConvertersTest {

    @Test
    public void should_correctly_convert_to_hockey_score() {
        InternalTerms bucket1 = initScoresBucket("1991/1992", ImmutableMap.of(1L, 1L, 2L, 2L, 3L, 3L),
                mock(LongTerms.class));

        List<HockeyScoreDto> scores = AggregationConverters.toHockeyLists().apply(bucket1);

        assertThat(scores).hasSize(3);
        assertThat(scores).extracting("round").containsOnly(1L, 2L, 3L);
        assertThat(scores).extracting("games").containsOnly(1L, 2L, 3L);
    }

    public static InternalTerms initScoresBucket(String season, Map<Long, Long> roundGamesMap, InternalTerms aggregation) {
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsString()).thenReturn(season);
        Aggregations aggsBucket1 = mock(Aggregations.class);
        when(bucket1.getAggregations()).thenReturn(aggsBucket1);
        List<Terms.Bucket> buckets = new ArrayList<>();
        for (Map.Entry<Long, Long> roundGame : roundGamesMap.entrySet()) {
            Terms.Bucket goalsBucket = mock(Terms.Bucket.class);
            when(goalsBucket.getKeyAsNumber()).thenReturn(roundGame.getKey());
            when(goalsBucket.getDocCount()).thenReturn(roundGame.getValue());
            when(goalsBucket.getKeyAsString()).thenReturn(roundGame.getKey()+":"+roundGame.getValue());
            buckets.add(goalsBucket);
        }
        when(aggregation.getBuckets()).thenReturn(buckets);
        when(aggsBucket1.asList()).thenReturn(Lists.<Aggregation>newArrayList(aggregation));
        return aggregation;
    }

}
