package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.schema.Column;

public interface GroupBy<Source> {

    <R> Linq<Source> groupBy(Column... column);

    Linq<Source> having();
}
