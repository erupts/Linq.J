package xyz.erupt.linq.engine;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.It;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Dql;
import xyz.erupt.linq.schema.JoinSchema;
import xyz.erupt.linq.schema.Row;
import xyz.erupt.linq.util.Columns;
import xyz.erupt.linq.util.ReflectField;
import xyz.erupt.linq.util.RowUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Engine {

    public void preprocessor(Dql dql) {
        // no select -> select *: derive columns from the source element type
        if (dql.getColumns().isEmpty()) {
            this.deriveColumns(dql);
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

    private void deriveColumns(Dql dql) {
        if (null == dql.getFrom()) return;
        Object first = null;
        for (Object obj : dql.getFrom()) {
            if (null != obj) {
                first = obj;
                break;
            }
        }
        if (null == first) return; // empty source -> empty result, columns irrelevant
        Class<?> clazz = first.getClass();
        if (RowUtil.isSimpleClass(clazz)) {
            dql.getColumns().add(Columns.of(It::self));
        } else {
            for (Field field : ReflectField.getFields(clazz)) {
                dql.getColumns().add(new Column(clazz, field.getName(), field.getName()));
            }
        }
    }


    public abstract List<Row> query(Dql dql);

    /**
     * Optional single-table fast path: transform source objects straight into the target type,
     * skipping Row materialization entirely. Return {@code null} when the query shape requires
     * the full Row pipeline — callers must then fall back to {@link #query(Dql)}.
     */
    public <T> List<T> queryDirect(Dql dql, Class<T> target) {
        return null;
    }

    /** Map-shaped variant of {@link #queryDirect(Dql, Class)}; same fallback contract. */
    public List<Map<String, Object>> queryDirectMap(Dql dql) {
        return null;
    }

}
