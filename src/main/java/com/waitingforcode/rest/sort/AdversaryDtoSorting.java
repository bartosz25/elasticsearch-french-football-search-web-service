package com.waitingforcode.rest.sort;

import com.waitingforcode.rest.dto.score.AdversaryDto;

import java.util.Comparator;

public enum AdversaryDtoSorting implements Comparator<AdversaryDto> {

    SCORED_GOALS_DESC {
        @Override
        public int compare(AdversaryDto adversary1, AdversaryDto adversary2) {
            if (adversary1.getGoals() == adversary2.getGoals()) return 0;
            return adversary1.getGoals() > adversary2.getGoals() ? -1 : 1;
        }
    }

}
