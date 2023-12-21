package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.util.Columns;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Where {


    default <R> Linq between(SFunction<R, ?> column, Object start, Object end) {
        return null;
    }

    //equals
    default <R> Linq eq(SFunction<R, ?> column, Object value) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> value.equals(f.get(c)));
    }

    //not equals
    default <R> Linq ne(SFunction<R, ?> column, Object value) {
        return null;
    }

    // >=
    default <R> Linq ge(SFunction<R, ?> column, Object value) {
        return null;
    }

    // <=
    default <R> Linq le(SFunction<R, ?> column, Object value) {
        return null;
    }

    // >
    default <R> Linq gt(SFunction<R, ?> column, Object value) {
        return null;
    }

    // <
    default <R> Linq lt(SFunction<R, ?> column, Object value) {
        return null;
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
