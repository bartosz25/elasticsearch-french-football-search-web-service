package com.waitingforcode.elasticsearch.comparator;

import com.google.common.collect.Lists;
import com.waitingforcode.rest.dto.score.AdversaryDto;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AdversaryComparatorsTest {

    @Test
    public void should_correctly_sort_adversaries_with_different_names() {
        AdversaryDto rcLens = new AdversaryDto("RC Lens", 10, 10);
        AdversaryDto lilleOsc = new AdversaryDto("Lille OSC", 20, 20);
        AdversaryDto acAjaccio = new AdversaryDto("AC Ajaccio", 5, 5);
        List<AdversaryDto> adversaries = Lists.newArrayList(rcLens, lilleOsc, acAjaccio);

        Collections.sort(adversaries, AdversaryComparators.SCORED_GOALS);

        assertThat(adversaries).extracting("team").containsExactly("Lille OSC", "RC Lens", "AC Ajaccio");
    }

    @Test
    public void should_correctly_sort_adversaries_with_the_same_number_of_goals() {
        AdversaryDto ajAuxerre = new AdversaryDto("AJ Auxerre", 10, 10);
        AdversaryDto rcLens = new AdversaryDto("RC Lens", 10, 10);
        AdversaryDto lilleOsc = new AdversaryDto("Lille OSC", 10, 10);
        AdversaryDto acAjaccio = new AdversaryDto("AC Ajaccio", 5, 5);
        List<AdversaryDto> adversaries = Lists.newArrayList(ajAuxerre, rcLens, lilleOsc, acAjaccio);

        Collections.sort(adversaries, AdversaryComparators.SCORED_GOALS);

        assertThat(adversaries).extracting("team").containsExactly("AJ Auxerre", "Lille OSC", "RC Lens", "AC Ajaccio");

    }

}
