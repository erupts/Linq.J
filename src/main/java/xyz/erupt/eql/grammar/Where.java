package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.consts.CompareSymbol;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.util.Columns;
import xyz.erupt.eql.util.CompareUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Where {

    //equals
    default <R> Linq eq(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> null != value && value.equals(f.get(c)));
    }

    //not equals
    default <R> Linq ne(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> null != value && !value.equals(f.get(c)));
    }


    // :val >= start and :val <= end
    default <R> Linq between(SFunction<R, ?> column, Object start, Object end) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> CompareUtil.compare(start, f.get(c), CompareSymbol.LTE) &&
                CompareUtil.compare(end, f.get(c), CompareSymbol.GTE));
    }

    // >
    default <R> Linq gt(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> CompareUtil.compare(value, f.get(c), CompareSymbol.GT));
    }

    // >=
    default <R> Linq gte(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> CompareUtil.compare(value, f.get(c), CompareSymbol.GTE));
    }

    // <
    default <R> Linq lt(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> CompareUtil.compare(value, f.get(c), CompareSymbol.LT));
    }

    // <=
    default <R> Linq lte(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> CompareUtil.compare(value, f.get(c), CompareSymbol.LTE));
    }

    default <R> Linq like(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null && value != null && f.get(c).toString().contains(value.toString()));
    }

    //in
    default <R> Linq in(SFunction<R, ?> column, Object... value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null && Arrays.stream(value).anyMatch(it -> null != it && it.equals(f.get(c))));
    }

    default <R> Linq in(SFunction<R, ?> column, List<Object> value) {
        return in(column, value.toArray());
    }

    //not in
    default <R> Linq notIn(SFunction<R, ?> column, Object... value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null && Arrays.stream(value).noneMatch(it -> null != it && it.equals(f.get(c))));
    }

    default <R> Linq notIn(SFunction<R, ?> column, List<Object> value) {
        return notIn(column, value.toArray());
    }


    default <R> Linq isNull(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) == null);
    }

    default <R> Linq isNotNull(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null);
    }

    default <R> Linq isBlank(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) == null || f.get(c).toString().trim().isEmpty());
    }

    default <R> Linq isNotBlank(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null && !f.get(c).toString().trim().isEmpty());
    }

    <R> Linq condition(Column<R> column, Function<Map<Column<?>, ?>, Boolean> fun);

}
