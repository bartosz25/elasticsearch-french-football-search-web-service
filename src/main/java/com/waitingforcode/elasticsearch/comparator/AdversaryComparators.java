package com.waitingforcode.elasticsearch.comparator;

import com.google.common.collect.ComparisonChain;
import com.waitingforcode.rest.dto.score.AdversaryDto;

import java.util.Comparator;


public enum AdversaryComparators implements Comparator<AdversaryDto> {

    SCORED_GOALS {
        @Override
        public int compare(AdversaryDto adversary1, AdversaryDto adversary2) {
            return ComparisonChain.start()
                    .compare(adversary2.getGoals(), adversary1.getGoals())
                    .compare(adversary1.getTeam(), adversary2.getTeam())
                    .result();
        }
    };

}
