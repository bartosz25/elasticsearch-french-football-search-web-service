package com.waitingforcode.elasticsearch.consumer.impl;

import com.waitingforcode.elasticsearch.comparator.AdversaryComparators;
import com.waitingforcode.elasticsearch.config.SearchDictionary;
import com.waitingforcode.elasticsearch.consumer.ScoreConsumer;
import com.waitingforcode.elasticsearch.data.ScoreTypes;
import com.waitingforcode.elasticsearch.query.aggs.StringTermsAggs;
import com.waitingforcode.elasticsearch.transformer.ScoreTransformers;
import com.waitingforcode.elasticsearch.util.InternalSumHelper;
import com.waitingforcode.rest.converter.SearchHitConverters;
import com.waitingforcode.rest.converter.TermBucketConverters;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.BaseScoreDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import com.waitingforcode.rest.dto.score.PlacesByScoredGoalsDto;
import com.waitingforcode.rest.dto.score.ScoreStatsDto;
import com.waitingforcode.rest.dto.score.SeasonScoreDto;
import com.waitingforcode.rest.dto.score.StatsDto;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ScoreConsumerImpl implements ScoreConsumer {

    @Override
    public int getMaxScoredGoals(SearchResponse response, String teamName) {
        StringTerms groupGoals = StringTermsAggs.GROUPED_BY_TEAMS.getTerm(response);
        Map<String, Aggregation> aggs = groupGoals.getBucketByKey(teamName).getAggregations().getAsMap();
        InternalFilter filterHost = (InternalFilter) aggs.get("scores_home");
        InternalMax hostGoals = filterHost.getAggregations().get(SearchDictionary.GOALS);
        InternalFilter filterGuest = (InternalFilter) aggs.get("scores_away");
        InternalMax guestGoals = filterGuest.getAggregations().get(SearchDictionary.GOALS);

        return hostGoals.getValue() > guestGoals.getValue() ? (int) hostGoals.getValue() : (int) guestGoals.getValue();

    }

    @Override
    public ScoreStatsDto getStatsForScoreType(List<SearchResponse> responses, ScoreTypes scoreType) {
        ScoreStatsDto scoreStats = new ScoreStatsDto(scoreType);
        for (int i = 0; i < responses.size(); i++) {
            scoreStats.addScores(new ScoreStatsDto.Match(i, (int) responses.get(i).getHits().getTotalHits()));
        }
        return scoreStats;
    }

    @Override
    public StatsDto getGoalsBySeason(SearchResponse response, StatsDto scoreStats) {
        StringTermsAggs.GROUPED_BY_SEASON.getTerm(response)
                .getBuckets().forEach(new GoalsStatsConsumer(scoreStats));
        return scoreStats;
    }

    @Override
    public Map<String, List<BaseScoreDto>> getMostPopularScored(SearchResponse searchResponse) {
        return StringTermsAggs.GROUPED_BY_SEASON.getTerm(searchResponse)
                .getBuckets().stream()
                .collect(Collectors.toMap(bucket -> bucket.getKeyAsString(), ScoreTransformers.bucketToScores()));
    }

    @Override
    public List<AdversaryDto> getBestScorerAdversaries(SearchResponse searchResponse) {
        return StringTermsAggs.GROUPED_BY_TEAMS.getTerm(searchResponse).getBuckets().stream()
                .map(TermBucketConverters.toAdversary())
                .sorted(AdversaryComparators.SCORED_GOALS)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<HockeyScoreDto>> getHockeyScores(SearchResponse searchResponse) {
        return StringTermsAggs.GROUPED_BY_SEASON.getTerm(searchResponse)
                .getBuckets().stream()
                .collect(Collectors.toMap(bucket -> bucket.getKeyAsString(), ScoreTransformers.bucketToHockeyScores()));
    }

    @Override
    public List<PlacesByScoredGoalsDto> getPlacesBySeason(SearchResponse searchResponse, Map<String, Long> seasons) {
        return Stream.of(searchResponse.getHits().getHits())
                .map(SearchHitConverters.toPlacesConverter(seasons))
                .collect(Collectors.toList());
    }

    private static class GoalsStatsConsumer implements Consumer<Terms.Bucket> {
        private StatsDto scoreStats;

        public GoalsStatsConsumer(StatsDto scoreStats) {
            this.scoreStats = scoreStats;
        }

        @Override
        public void accept(Terms.Bucket bucket) {
            String season = bucket.getKeyAsString();
            Map<String, Integer> goals = bucket.<Aggregation>getAggregations().asList().stream()
                    .collect(Collectors.toMap(
                            aggregation -> aggregation.getName(),
                            aggregation -> InternalSumHelper
                                    .getInt((InternalSum) ((InternalFilter)aggregation).getAggregations().asList().get(0))
                    ));
            scoreStats.addGoal(season, SeasonScoreDto.valueOf(goals));
        }
    }

}
