package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Select {

    Linq distinct();

    Linq select(Column... columns);

    // select *
    <T> Linq select(Class<T> table);

    // select a, b, c
    <T> Linq select(SFunction<T, ?>... columns);

    // select fn(x)
    <T, F> Linq select(SFunction<T, F> column, BiFunction<Row, F, Object> convert);

    // select a as n
    <T> Linq selectAs(SFunction<T, ?> column, String alias);

    // select a as fun
    <T, A> Linq selectAs(SFunction<T, ?> column, SFunction<A, ?> alias);

    // select fn(x) as n
    <T, F> Linq selectAs(SFunction<T, F> column, BiFunction<Row, F, Object> convert, String alias);

    // select fn(x) as fun
    <T, A, F> Linq selectAs(SFunction<T, F> column, BiFunction<Row, F, Object> convert, SFunction<A, ?> alias);

    Linq selectRowAs(Function<Row, Object> convert, String alias);

    <A> Linq selectRowAs(Function<Row, Object> convert, SFunction<A, ?> alias);

    <T> Linq selectExclude(SFunction<T, ?>... columns);

}
