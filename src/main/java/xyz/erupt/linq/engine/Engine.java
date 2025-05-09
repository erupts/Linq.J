package xyz.erupt.linq.engine;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.schema.Dql;
import xyz.erupt.linq.schema.JoinSchema;
import xyz.erupt.linq.schema.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Engine {

    public void preprocessor(Dql dql) {
        if (dql.getColumns().isEmpty()) {
            throw new LinqException("Missing select definition");
        }
        // join check
        Map<Class<?>, Void> joinMap = new HashMap<>();
        for (JoinSchema<?> schema : dql.getJoinSchemas()) {
            if (joinMap.containsKey(schema.getClazz())) {
                throw new LinqException("The same object join is not supported " + " → " + schema.getClazz().getSimpleName());
            }
            joinMap.put(schema.getClazz(), null);
        }
    }


    public abstract List<Row> query(Dql dql);

}
