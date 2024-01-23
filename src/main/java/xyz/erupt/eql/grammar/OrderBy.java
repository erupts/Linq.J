package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.lambda.SFunction;

public interface OrderBy {

    <R> Linq orderBy(SFunction<R, ?> column, Direction direction);


    default <R> Linq orderBy(SFunction<R, ?> column) {
        return orderBy(column, Direction.ASC);
    }

    default <R> Linq orderByAsc(SFunction<R, ?> column) {
        return orderBy(column);
    }

    default <R> Linq orderByDesc(SFunction<R, ?> column) {
        return orderBy(column, Direction.DESC);
    }

    public enum Direction {

        ASC,
        DESC

    }

}
