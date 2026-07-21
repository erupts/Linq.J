package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface GroupBy {

    Linq groupBy(Column... columns);

    <T> Linq groupBy(SFunction<T, ?>... columns);

    <T, F> Linq groupBy(SFunction<T, F> column, BiFunction<Row, F, Object> convert);

    Linq having(Predicate<Row> condition);

    default <R, S> Linq having(SFunction<R, S> column, Predicate<S> condition) {
        return having(row -> condition.test(row.get(column)));
    }

    // having on an aggregate alias, e.g. having("total", v -> ((Number) v).intValue() > 10)
    default Linq having(String alias, Predicate<Object> condition) {
        return having(row -> condition.test(row.get(alias)));
    }

}
