package com.waitingforcode.elasticsearch.consumer.impl;

import com.google.common.collect.Lists;
import com.waitingforcode.elasticsearch.comparator.ScoreComparators;
import com.waitingforcode.elasticsearch.consumer.SeasonConsumer;
import com.waitingforcode.elasticsearch.query.aggs.StringTermsAggs;
import com.waitingforcode.elasticsearch.transformer.SeasonTransformers;
import com.waitingforcode.rest.dto.score.SeasonDto;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SeasonConsumerImpl implements SeasonConsumer {

    @Override
    public List<SeasonDto> getSeasonsFromResponse(SearchResponse response) {
        List<SeasonDto> seasons = Lists.newArrayList(response.getHits().getHits())
                .stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), SeasonTransformers.fromSearchHitsToSeasons()));
        seasons.forEach(season -> season.getScores().sort(ScoreComparators.GLOBAL));
        return seasons;
    }

    @Override
    public Map<String, Long> getSeasonsWithDocOccurrence(SearchResponse response) {
        return StringTermsAggs.GROUPED_BY_SEASON.getTerm(response).getBuckets().stream()
                    .collect(Collectors.toMap(bucket -> bucket.getKeyAsString(), Terms.Bucket::getDocCount));
    }

}
