package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.lambda.SFunction;

public interface OrderBy {

    <R> Linq orderBy(SFunction<R, ?> column, Direction direction);


    default <R> Linq orderBy(SFunction<R, ?> column) {
        return orderBy(column, Direction.ASC);
    }

    public enum Direction {

        ASC,
        DESC

    }

}
