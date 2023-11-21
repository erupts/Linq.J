package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.schema.Column;

public interface GroupBy<Source> {

    <R> Linq<Source> groupBy(Column... column);

    Linq<Source> having();
}
