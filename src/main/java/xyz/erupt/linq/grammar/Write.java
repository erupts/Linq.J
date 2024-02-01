package xyz.erupt.linq.grammar;

import xyz.erupt.linq.engine.Engine;
import xyz.erupt.linq.schema.Dql;
import xyz.erupt.linq.schema.Row;
import xyz.erupt.linq.util.ColumnReflects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface Write {

    Engine $engine();

    Dql $dql();

    default <T> List<T> write(Class<T> clazz) {
        $engine().syntaxCheck(this.$dql());
        List<Row> table = $engine().query(this.$dql());
        return table.stream().map(it -> ColumnReflects.rowToObject(it, clazz)).collect(Collectors.toList());
    }

    default <T> T writeOne(Class<T> clazz) {
        $engine().syntaxCheck(this.$dql());
        List<Row> table = $engine().query(this.$dql());
        if (table.isEmpty()) {
            return null;
        } else {
            return ColumnReflects.rowToObject(table.get(0), clazz);
        }
    }

    default List<Map<String, Object>> writeMap() {
        $engine().syntaxCheck(this.$dql());
        List<Row> table = $engine().query(this.$dql());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Row row : table) {
            Map<String, Object> $map = new HashMap<>();
            result.add($map);
            row.forEach((k, v) -> $map.put(k.getAlias(), v));
        }
        return result;
    }


}
