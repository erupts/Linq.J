package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.schema.Column;

public interface GroupBy {

    <R> Linq groupBy(Column<?>... column);

    Linq having();
}
