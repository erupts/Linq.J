package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.schema.Column;

public interface GroupBy {

    Linq groupBy(Column... columns);

//    Linq having();

}
