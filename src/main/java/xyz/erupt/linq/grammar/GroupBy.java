package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface GroupBy {

    Linq groupBy(Column... columns);

    <T> Linq groupBy(SFunction<T, ?>... columns);

    <T, F> Linq groupBy(SFunction<T, F> column, BiFunction<Row, F, Object> convert);

    Linq having(Function<Row, Boolean> condition);

    default <R, S> Linq having(SFunction<R, S> fun, Function<S, Boolean> condition) {
        return having(row -> condition.apply(row.get(fun)));
    }

}
