package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.schema.Column;

public interface Select<Source> {

    Linq<Source> distinct();

//    Linq<Source> select(SFunction<Source, ?> column);
//
//    <R> Linq<Source> select(R r, SFunction<R, ?> column);

    Linq<Source> select(Column... column);


}
