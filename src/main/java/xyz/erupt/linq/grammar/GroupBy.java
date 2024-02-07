package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.lambda.SFunction;

public interface GroupBy {

    <T> Linq groupBy(SFunction<T, ?> column, SFunction<T, ?>... columns);

//    Linq having(Function<Row, Boolean>... fun);

}
