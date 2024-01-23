package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.schema.Column;

public interface GroupBy {

    Linq groupBy(Column<?>... columns);

//    Linq having();
}
