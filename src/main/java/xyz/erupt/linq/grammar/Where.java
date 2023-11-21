package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.fun.SFunction;
import xyz.erupt.linq.schema.Column;

import java.util.Map;
import java.util.function.Function;

public interface Where<SOURCE> {


    default <R> Linq<SOURCE> between(SFunction<R, ?> column, Object start, Object end) {
        return null;
    }

    //equals
    default <R> Linq<SOURCE> eq(SFunction<R, ?> column, Object value) {
        return null;
    }

    //not equals
    default <R> Linq<SOURCE> ne(SFunction<R, ?> column, Object value) {
        return null;
    }

    // >=
    default <R> Linq<SOURCE> ge(SFunction<R, ?> column, Object value) {
        return null;
    }

    // <=
    default <R> Linq<SOURCE> le(SFunction<R, ?> column, Object value) {
        return null;
    }

    // >
    default <R> Linq<SOURCE> gt(SFunction<R, ?> column, Object value) {
        return null;
    }

    // <
    default <R> Linq<SOURCE> lt(SFunction<R, ?> column, Object value) {
        return null;
    }

    default <R> Linq<SOURCE> like(SFunction<R, ?> column, Object value) {
        return null;
    }

    //in
    default <R> Linq<SOURCE> in(SFunction<R, ?> column, Object... value) {
        return null;
    }


    //not in
    default <R> Linq<SOURCE> notIn(SFunction<R, ?> column, Object... value) {
        return null;
    }


    default <R> Linq<SOURCE> isNull(SFunction<R, ?> column) {
        Column c = Column.fromLambda(column);
        return condition(Column.of(column), (f) -> f.get(c) == null);
    }

    default <R> Linq<SOURCE> isNotNull(SFunction<R, ?> column) {
        Column c = Column.fromLambda(column);
        return condition(Column.of(column), (f) -> f.get(c) != null);
    }

    default <R> Linq<SOURCE> isNotBlank(SFunction<R, ?> column) {
        Column c = Column.fromLambda(column);
        return condition(Column.of(column), (f) -> f.get(c) != null && !f.get(c).toString().trim().isEmpty());
    }

    <R> Linq<SOURCE> condition(Column column, Function<Map<Column, ?>, Boolean> fun);

}
