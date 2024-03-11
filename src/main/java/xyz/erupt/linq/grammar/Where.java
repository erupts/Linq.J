package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.consts.CompareSymbol;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;
import xyz.erupt.linq.util.Columns;
import xyz.erupt.linq.util.CompareUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface Where {

    // equals
    default <R> Linq eq(SFunction<R, ?> column, Object value) {
        return where(column, f -> null != value && value.equals(f));
    }

    // not equals
    default <R> Linq ne(SFunction<R, ?> column, Object value) {
        return where(column, f -> null != value && !value.equals(f));
    }


    // :val >= start and :val <= end
    default <R> Linq between(SFunction<R, ?> column, Object start, Object end) {
        return where(column, row -> CompareUtil.compare(row, start, CompareSymbol.GTE) &&
                CompareUtil.compare(row, end, CompareSymbol.LTE));
    }

    // >
    default <R> Linq gt(SFunction<R, ?> column, Object value) {
        return where(column, f -> CompareUtil.compare(f, value, CompareSymbol.GT));
    }

    // <
    default <R> Linq lt(SFunction<R, ?> column, Object value) {
        return where(column, f -> CompareUtil.compare(f, value, CompareSymbol.LT));
    }

    // >=
    default <R> Linq gte(SFunction<R, ?> column, Object value) {
        return where(column, f -> CompareUtil.compare(f, value, CompareSymbol.GTE));
    }

    // <=
    default <R> Linq lte(SFunction<R, ?> column, Object value) {
        return where(column, f -> CompareUtil.compare(f, value, CompareSymbol.LTE));
    }

    default <R> Linq like(SFunction<R, ?> column, Object value) {
        return where(column, f -> f != null && value != null && f.toString().contains(value.toString()));
    }

    default <R> Linq in(SFunction<R, ?> column, Object... value) {
        return where(column, f -> f != null && Arrays.stream(value).anyMatch(it -> null != it && it.equals(f)));
    }

    default <R> Linq in(SFunction<R, ?> column, List<Object> value) {
        return in(column, value.toArray());
    }

    default <R> Linq notIn(SFunction<R, ?> column, Object... value) {
        return where(column, f -> f != null && Arrays.stream(value).noneMatch(it -> null != it && it.equals(f)));
    }

    default <R> Linq notIn(SFunction<R, ?> column, List<Object> value) {
        return notIn(column, value.toArray());
    }


    default <R> Linq isNull(SFunction<R, ?> column) {
        return where(column, Objects::isNull);
    }

    default <R> Linq isNotNull(SFunction<R, ?> column) {
        return where(column, Objects::nonNull);
    }

    default <R> Linq isBlank(SFunction<R, ?> column) {
        return where(column, f -> f == null || f.toString().trim().isEmpty());
    }

    default <R> Linq isNotBlank(SFunction<R, ?> column) {
        return where(column, f -> f != null && !f.toString().trim().isEmpty());
    }

    default <R, S> Linq where(SFunction<R, S> column, Function<S, Boolean> process) {
        return where(f -> process.apply(f.get(column)));
    }

    Linq where(Function<Row, Boolean> fun);

}
