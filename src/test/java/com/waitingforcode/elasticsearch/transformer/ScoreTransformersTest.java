package com.waitingforcode.elasticsearch.transformer;

import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ScoreTransformersTest {

    @Test
    public void should_correctly_convert_to_basic_score() {
        List<Terms.Bucket> buckets = new ArrayList<>();
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsString()).thenReturn("2:1");
        when(bucket1.getDocCount()).thenReturn(3L);
        Terms.Bucket bucket2 = mock(Terms.Bucket.class);
        when(bucket2.getKeyAsString()).thenReturn("3:1");
        when(bucket2.getDocCount()).thenReturn(5L);
        StringTerms aggregation = mock(StringTerms.class);
        buckets.add(bucket1);
        buckets.add(bucket2);
        when(aggregation.getBuckets()).thenReturn(buckets);

        List<BaseScoreDto> scores = ScoreTransformers.aggregatorToBaseScore().apply(aggregation);

        assertThat(scores).hasSize(2);
        assertThat(scores).extracting("score").containsOnly("2:1", "3:1");
        assertThat(scores).extracting("games").containsOnly(3, 5);
    }

    @Test
    public void should_correctly_convert_to_a_list_of_scores() {
        Terms.Bucket mainBucket = mock(Terms.Bucket.class);
        List<Terms.Bucket> buckets = new ArrayList<>();
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsString()).thenReturn("2:1");
        when(bucket1.getDocCount()).thenReturn(3L);
        Terms.Bucket bucket2 = mock(Terms.Bucket.class);
        when(bucket2.getKeyAsString()).thenReturn("3:1");
        when(bucket2.getDocCount()).thenReturn(5L);
        StringTerms aggregation = mock(StringTerms.class);
        buckets.add(bucket1);
        buckets.add(bucket2);
        when(aggregation.getBuckets()).thenReturn(buckets);
        Aggregations aggs = mock(Aggregations.class);
        when(aggs.asList()).thenReturn(Collections.<Aggregation>singletonList(aggregation));
        when(mainBucket.getAggregations()).thenReturn(aggs);

        List<BaseScoreDto> scores = ScoreTransformers.bucketToScores().apply(mainBucket);

        assertThat(scores).hasSize(2);
        assertThat(scores).extracting("score").containsOnly("2:1", "3:1");
        assertThat(scores).extracting("games").containsOnly(3, 5);

    }

    @Test
    public void should_correctly_convert_to_a_list_of_hockey_scores() {
        Terms.Bucket mainBucket = mock(Terms.Bucket.class);
        List<Terms.Bucket> buckets = new ArrayList<>();
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsNumber()).thenReturn(2L);
        when(bucket1.getDocCount()).thenReturn(3L);
        Terms.Bucket bucket2 = mock(Terms.Bucket.class);
        when(bucket2.getKeyAsNumber()).thenReturn(4L);
        when(bucket2.getDocCount()).thenReturn(5L);
        LongTerms aggregation = mock(LongTerms.class);
        buckets.add(bucket1);
        buckets.add(bucket2);
        when(aggregation.getBuckets()).thenReturn(buckets);
        Aggregations aggs = mock(Aggregations.class);
        when(aggs.asList()).thenReturn(Collections.<Aggregation>singletonList(aggregation));
        when(mainBucket.getAggregations()).thenReturn(aggs);

        List<HockeyScoreDto> scores = ScoreTransformers.bucketToHockeyScores().apply(mainBucket);

        assertThat(scores).hasSize(2);
        assertThat(scores).extracting("round").containsOnly(2L, 4L);
        assertThat(scores).extracting("games").containsOnly(3L, 5L);
    }

}
