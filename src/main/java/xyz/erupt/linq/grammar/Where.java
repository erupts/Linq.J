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
import java.util.function.Function;

public interface Where {

    // equals
    default <R> Linq eq(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> null != value && value.equals(f.get(col)));
    }

    // not equals
    default <R> Linq ne(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> null != value && !value.equals(f.get(col)));
    }


    // :val >= start and :val <= end
    default <R> Linq between(SFunction<R, ?> column, Object start, Object end) {
        Column col = Columns.of(column);
        return where(row -> CompareUtil.compare(row.get(col), start, CompareSymbol.GTE) &&
                CompareUtil.compare(row.get(col), end, CompareSymbol.LTE));
    }

    // >
    default <R> Linq gt(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> CompareUtil.compare(f.get(col), value, CompareSymbol.GT));
    }

    // <
    default <R> Linq lt(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> CompareUtil.compare(f.get(col), value, CompareSymbol.LT));
    }

    // >=
    default <R> Linq gte(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> CompareUtil.compare(f.get(col), value, CompareSymbol.GTE));
    }

    // <=
    default <R> Linq lte(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> CompareUtil.compare(f.get(col), value, CompareSymbol.LTE));
    }

    default <R> Linq like(SFunction<R, ?> column, Object value) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) != null && value != null && f.get(col).toString().contains(value.toString()));
    }

    default <R> Linq in(SFunction<R, ?> column, Object... value) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) != null && Arrays.stream(value).anyMatch(it -> null != it && it.equals(f.get(col))));
    }

    default <R> Linq in(SFunction<R, ?> column, List<Object> value) {
        return in(column, value.toArray());
    }

    default <R> Linq notIn(SFunction<R, ?> column, Object... value) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) != null && Arrays.stream(value).noneMatch(it -> null != it && it.equals(f.get(col))));
    }

    default <R> Linq notIn(SFunction<R, ?> column, List<Object> value) {
        return notIn(column, value.toArray());
    }


    default <R> Linq isNull(SFunction<R, ?> column) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) == null);
    }

    default <R> Linq isNotNull(SFunction<R, ?> column) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) != null);
    }

    default <R> Linq isBlank(SFunction<R, ?> column) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) == null || f.get(col).toString().trim().isEmpty());
    }

    default <R> Linq isNotBlank(SFunction<R, ?> column) {
        Column col = Columns.of(column);
        return where(f -> f.get(col) != null && !f.get(col).toString().trim().isEmpty());
    }

    Linq where(Function<Row, Boolean> fun);

}
