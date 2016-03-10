package com.waitingforcode.rest.http.params;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SeasonPathParamTest {

    @Test
    public void should_correctly_construct_object_with_factory_method() {
        SeasonPathParam param = new SeasonPathParam("2002_2003");

        assertThat(param.getQueryForm()).isEqualTo("2002/2003");
        assertThat(param.getStartYear()).isEqualTo(2002);
        assertThat(param.getEndYear()).isEqualTo(2003);
    }

}
