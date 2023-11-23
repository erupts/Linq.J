package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.schema.Column;

public interface Select<Source> {

    Linq<Source> distinct();

    Linq<Source> select(Column... column);


}
