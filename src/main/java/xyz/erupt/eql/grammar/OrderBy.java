package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.fun.SFunction;

public interface OrderBy<Source> {

    <R> Linq<Source> orderBy(SFunction<R, ?> column, Direction direction);


    default <R> Linq<Source> orderBy(SFunction<R, ?> column) {
        return orderBy(column, Direction.ASC);
    }

    public enum Direction {

        ASC,
        DESC

    }

}
