package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.util.Columns;

import java.util.Map;
import java.util.function.Function;

public interface Where {


    default <R> Linq between(SFunction<R, ?> column, Object start, Object end) {
        return null;
    }

    //equals
    default <R> Linq eq(SFunction<R, ?> column, Object value) {
        return null;
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
        return null;
    }

    //in
    default <R> Linq in(SFunction<R, ?> column, Object... value) {
        return null;
    }


    //not in
    default <R> Linq notIn(SFunction<R, ?> column, Object... value) {
        return null;
    }


    default <R> Linq isNull(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) == null);
    }

    default <R> Linq isNotNull(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null);
    }

    default <R> Linq isNotBlank(SFunction<R, ?> column) {
        Column<R> c = Columns.fromLambda(column);
        return condition(Columns.of(column), (f) -> f.get(c) != null && !f.get(c).toString().trim().isEmpty());
    }

    <R> Linq condition(Column<R> column, Function<Map<Column<?>, ?>, Boolean> fun);

}
