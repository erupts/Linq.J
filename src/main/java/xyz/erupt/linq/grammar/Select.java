package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.schema.Column;

public interface Select {

    Linq distinct();

    Linq select(Column... column);


}
