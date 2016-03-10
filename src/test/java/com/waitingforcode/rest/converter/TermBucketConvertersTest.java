package com.waitingforcode.rest.converter;

import com.waitingforcode.elasticsearch.consumer.impl.ScoreConsumerImplTest;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import com.waitingforcode.rest.dto.score.HockeyScoreDto;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TermBucketConvertersTest {

    @Test
    public void should_correctly_convert_to_adversary() {
        Terms.Bucket bucket = ScoreConsumerImplTest.initAdversary("AS Cannes", 10, 15);

        AdversaryDto adversaryDto = TermBucketConverters.toAdversary().apply(bucket);

        assertThat(adversaryDto.getGoals()).isEqualTo(25);
        assertThat(adversaryDto.getTeam()).isEqualTo("AS Cannes");
        assertThat(adversaryDto.getScoredAsHost()).isEqualTo(10);
        assertThat(adversaryDto.getScoredAsGuest()).isEqualTo(15);
    }

    @Test
    public void should_correctly_convert_to_hockey_score() {
        Terms.Bucket bucket = mock(Terms.Bucket.class);
        when(bucket.getKeyAsNumber()).thenReturn(5L);
        when(bucket.getDocCount()).thenReturn(12L);

        HockeyScoreDto hockeyScoreDto = TermBucketConverters.toHockeyScore().apply(bucket);

        assertThat(hockeyScoreDto.getGames()).isEqualTo(12L);
        assertThat(hockeyScoreDto.getRound()).isEqualTo(5L);
    }

}
