package com.waitingforcode.elasticsearch.query.filters;


import org.elasticsearch.index.query.RangeQueryBuilder;

public enum RangeModes {

    GTE {
        @Override
        public RangeQueryBuilder get(RangeQueryBuilder builder, int value) {
                return builder.gte(value);
        }
    },
    GT {
        @Override
        public RangeQueryBuilder get(RangeQueryBuilder builder, int value) {
            return builder.gt(value);
        }
    },
    LTE {
        @Override
        public RangeQueryBuilder get(RangeQueryBuilder builder, int value) {
            return builder.lte(value);
        }
    },
    LT {
        @Override
        public RangeQueryBuilder get(RangeQueryBuilder builder, int value) {
            return builder.lt(value);
        }
    },
    EQ {
        @Override
        public RangeQueryBuilder get(RangeQueryBuilder builder, int value) {
            return builder.from(value).to(value);
        }
    };

    public abstract RangeQueryBuilder get(RangeQueryBuilder builder, int value);

}
