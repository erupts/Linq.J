package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.fun.SFunction;

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
