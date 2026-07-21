package xyz.erupt.linq.util;

import xyz.erupt.linq.consts.CompareSymbol;
import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Columns {

    public static <R> Column of(SFunction<R, ?> fun) {
        return of(fun, LambdaSee.field(fun));
    }

    public static <R> Column of(SFunction<R, ?> fun, String alias) {
        LambdaInfo lambdaInfo = LambdaSee.info(fun);
        Column column = new Column();
        column.setTable(lambdaInfo.getClazz());
        column.setField(lambdaInfo.getField());
        column.setAlias(alias);
        return column;
    }

    public static <R, A> Column of(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return of(fun, LambdaSee.field(alias));
    }

    public static Column count(String alias) {
        Column column = new Column(VirtualColumn.class, VirtualColumn.lambdaColumn().getField(), alias);
        column.setGroupByFun(it -> BigDecimal.valueOf(it.size()));
        return column;
    }

    public static <A> Column count(SFunction<A, ?> alias) {
        return count(LambdaSee.field(alias));
    }

    public static <R> Column count(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            int i = 0;
            for (Row row : list) {
                if (null != row.get(column)) i++;
            }
            return BigDecimal.valueOf(i);
        });
    }

    public static <R, A> Column count(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return count(fun, LambdaSee.field(alias));
    }

    public static <R> Column countDistinct(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            Map<Object, Void> distinctMap = new HashMap<>();
            for (Row row : list) {
                Optional.ofNullable(row.get(column)).ifPresent(it -> distinctMap.put(it, null));
            }
            return BigDecimal.valueOf(distinctMap.size());
        });
    }

    public static <R, A> Column countDistinct(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return countDistinct(fun, LambdaSee.field(alias));
    }

    public static <R> Column max(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            Object result = null;
            for (Row row : list) {
                Object val = row.get(column);
                if (null == result) result = val;
                if (CompareUtil.compare(val, result, CompareSymbol.GT)) result = val;
            }
            if (result instanceof Number) {
                return RowUtil.numberToBigDecimal((Number) result);
            } else {
                return result;
            }
        });
    }


    public static <R, A> Column max(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return max(fun, LambdaSee.field(alias));
    }

    public static <R> Column min(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            Object result = null;
            for (Row row : list) {
                Object val = row.get(column);
                if (null == result) result = val;
                if (CompareUtil.compare(val, result, CompareSymbol.LT)) result = val;
            }
            if (result instanceof Number) {
                return RowUtil.numberToBigDecimal((Number) result);
            } else {
                return result;
            }
        });
    }

    public static <R, A> Column min(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return min(fun, LambdaSee.field(alias));
    }

    public static <R> Column avg(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            NumberAccumulator acc = new NumberAccumulator();
            for (Row row : list) {
                Object val = row.get(column);
                if (val instanceof Number) acc.add((Number) val);
            }
            return acc.avg();
        });
    }

    public static <R, A> Column avg(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return avg(fun, LambdaSee.field(alias));
    }

    public static <R> Column sum(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            NumberAccumulator acc = new NumberAccumulator();
            for (Row row : list) {
                Object val = row.get(column);
                if (val instanceof Number) acc.add((Number) val);
            }
            return acc.sum();
        });
    }

    /**
     * Streaming numeric accumulator. Integral inputs ride a primitive {@code long} (with
     * overflow demotion); floating point and BigDecimal inputs demote to an exact BigDecimal
     * sum, matching the previous per-element BigDecimal semantics without its allocation cost.
     */
    private static final class NumberAccumulator {
        private long longSum;
        private BigDecimal bigSum; // non-null once demoted from the long fast path
        private int count;

        void add(Number n) {
            count++;
            if (null == bigSum && (n instanceof Integer || n instanceof Long || n instanceof Short || n instanceof Byte)) {
                long v = n.longValue();
                long r = longSum + v;
                if (((longSum ^ r) & (v ^ r)) < 0) { // overflow -> demote to BigDecimal
                    bigSum = BigDecimal.valueOf(longSum).add(BigDecimal.valueOf(v));
                } else {
                    longSum = r;
                }
            } else {
                if (null == bigSum) bigSum = BigDecimal.valueOf(longSum);
                // toString parse keeps the exact decimal semantics of the previous implementation
                bigSum = bigSum.add(n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString()));
            }
        }

        BigDecimal sum() {
            return null == bigSum ? BigDecimal.valueOf(longSum) : bigSum;
        }

        BigDecimal avg() {
            if (count == 0) return BigDecimal.valueOf(0);
            double total = null == bigSum ? (double) longSum : bigSum.doubleValue();
            return BigDecimal.valueOf(total / count);
        }
    }

    public static <R, A> Column sum(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return sum(fun, LambdaSee.field(alias));
    }

    // select object[]
    public static <R> Column groupArray(SFunction<R, ?> fun, String alias) {
        return aggregate(fun, alias, (column, list) -> {
            List<Object> result = new ArrayList<>();
            for (Row row : list) {
                Optional.ofNullable(row.get(column)).ifPresent(result::add);
            }
            return result;
        });
    }

    public static <R, A> Column groupArray(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return groupArray(fun, LambdaSee.field(alias));
    }

    // custom aggregation logic
    public static <R> Column aggregate(SFunction<R, ?> fun, String alias, BiFunction<Column, List<Row>, Object> process) {
        Column column = Columns.of(fun, alias);
        column.setGroupByFun(it -> process.apply(column.getRawColumn(), it));
        return column;
    }

}
