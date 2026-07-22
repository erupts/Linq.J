package xyz.erupt.linq.grammar;

import xyz.erupt.linq.engine.Engine;
import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.schema.Dql;
import xyz.erupt.linq.schema.Row;
import xyz.erupt.linq.util.RowUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Write {

    String MULTI_VAL_ERROR = "Expected one result (or null) to be returned by one(), but found: ";

    Engine engine();

    Dql dql();

    /** Terminal: materialize the result set as a list of {@code clazz}. */
    default <T> List<T> toList(Class<T> clazz) {
        engine().preprocessor(this.dql());
        // direct path first: object -> object without the Row intermediate; null means fall back
        List<T> direct = engine().queryDirect(this.dql(), clazz);
        if (null != direct) {
            return direct;
        }
        List<Row> table = engine().query(this.dql());
        if (table.isEmpty()) {
            return new ArrayList<>();
        }

        // Optimization: for simple types with single column, directly extract values
        Row firstRow = table.get(0);
        int firstRowSize = firstRow.size();
        if (firstRowSize == 1) {
            Object firstValue = firstRow.valueAt(0);

            // Check if it's a simple type that can be directly cast
            if (firstValue != null && clazz.isAssignableFrom(firstValue.getClass())) {
                List<T> result = new ArrayList<>(Math.min(table.size(), 10000));
                for (Row row : table) {
                    result.add((T) row.valueAt(0));
                }
                return result;
            }
            // Check if target is a simple type - use array access for better performance
            Class<?>[] simpleClasses = RowUtil.SIMPLE_CLASS;
            for (Class<?> simpleClass : simpleClasses) {
                if (simpleClass.isAssignableFrom(clazz)) {
                    List<T> result = new ArrayList<>(Math.min(table.size(), 10000));
                    for (Row row : table) {
                        result.add(RowUtil.rowToObject(row, clazz));
                    }
                    return result;
                }
            }
        }
        List<T> result = new ArrayList<>(Math.min(table.size(), 10000));
        for (Row row : table) {
            result.add(RowUtil.rowToObject(row, clazz));
        }
        return result;
    }

    /** Terminal: exactly one result expected — null when empty, throws when more than one. */
    default <T> T one(Class<T> clazz) {
        List<T> result = toList(clazz);
        if (result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new LinqException(MULTI_VAL_ERROR + result.size());
        }
    }

    /** Terminal: first result or null — never throws on multiple rows. */
    default <T> T first(Class<T> clazz) {
        Integer limit = this.dql().getLimit();
        if (null == limit || limit > 1) {
            this.dql().setLimit(1);
        }
        List<T> result = toList(clazz);
        return result.isEmpty() ? null : result.get(0);
    }

    /** Terminal: materialize each row as an alias-keyed map. */
    default List<Map<String, Object>> toMaps() {
        engine().preprocessor(this.dql());
        List<Map<String, Object>> direct = engine().queryDirectMap(this.dql());
        if (null != direct) {
            return direct;
        }
        List<Row> table = engine().query(this.dql());
        List<Map<String, Object>> result = new ArrayList<>(Math.min(table.size(), 10000));
        for (Row row : table) {
            int rowSize = row.size();
            Map<String, Object> $map = new HashMap<>(rowSize);
            result.add($map);
            for (int i = 0; i < rowSize; i++) {
                $map.put(row.columnAt(i).getAlias(), row.valueAt(i));
            }
        }
        return result;
    }

    /** Terminal: exactly one map expected — null when empty, throws when more than one. */
    default Map<String, Object> toMap() {
        List<Map<String, Object>> result = toMaps();
        if (result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new LinqException(MULTI_VAL_ERROR + result.size());
        }
    }

    /** Terminal: number of result rows. */
    default int count() {
        engine().preprocessor(this.dql());
        return engine().query(this.dql()).size();
    }

    /** Terminal: whether the query yields any row. */
    default boolean exists() {
        Integer limit = this.dql().getLimit();
        if (null == limit || limit > 1) {
            this.dql().setLimit(1);
        }
        return count() > 0;
    }

}
