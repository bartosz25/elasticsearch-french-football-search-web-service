package com.waitingforcode.elasticsearch.comparator;

import com.google.common.collect.ComparisonChain;
import com.waitingforcode.rest.dto.score.ScoreDto;

import java.util.Comparator;


public enum ScoreComparators implements Comparator<ScoreDto> {

    GLOBAL {
        @Override
        public int compare(ScoreDto score1, ScoreDto score2) {
            return ComparisonChain.start()
                    .compare(score1.getRound(), score2.getRound())
                    .compare(score1.getHostTeam().getName(), score2.getHostTeam().getName())
                    .compare(score1.getGuestTeam().getName(), score2.getGuestTeam().getName())
                    .result();
        }
    };

}
