package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.schema.Column;

public interface Select {

    Linq distinct();

    Linq select(Column... column);


}
