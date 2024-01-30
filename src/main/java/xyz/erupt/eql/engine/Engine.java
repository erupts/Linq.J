package xyz.erupt.eql.engine;

import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;

import java.util.List;
import java.util.Map;

public abstract class Engine {

    public void check(Dql dql) {
        if (dql.getColumns().isEmpty()) {
            throw new EqlException("Missing select definition");
        }
    }


    public abstract List<Map<Column<?>, Object>> query(Dql dql);

}
