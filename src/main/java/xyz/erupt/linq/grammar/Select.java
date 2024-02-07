package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;

public interface Select {

    Linq distinct();

    Linq select(Column column, Column... columns);

    <T> Linq select(SFunction<T, ?> column, SFunction<T, ?>... columns);

}
