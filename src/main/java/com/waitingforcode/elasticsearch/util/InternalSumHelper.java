package com.waitingforcode.elasticsearch.util;


import com.waitingforcode.elasticsearch.exception.ConstructorNotInvokableException;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;

public final class InternalSumHelper {

    private InternalSumHelper() {
        throw new ConstructorNotInvokableException();
    }

    public static int getInt(InternalSum internalSum) {
        return (int) internalSum.getValue();
    }

}
