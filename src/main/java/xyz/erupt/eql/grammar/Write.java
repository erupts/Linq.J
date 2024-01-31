package xyz.erupt.eql.grammar;

import xyz.erupt.eql.engine.Engine;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface Write {

    Engine $engine();

    Dql $dql();

    default <T> List<T> write(Class<T> clazz) {
        $engine().check(this.$dql());
        List<Map<Column, Object>> table = $engine().query(this.$dql());
        return table.stream().map(it -> ReflectUtil.convertMapToObject(it, clazz)).collect(Collectors.toList());
    }

    default <T> T writeOne(Class<T> clazz) {
        $engine().check(this.$dql());
        List<Map<Column, Object>> table = $engine().query(this.$dql());
        if (table.isEmpty()) {
            return null;
        } else {
            return ReflectUtil.convertMapToObject(table.get(0), clazz);
        }
    }

    default List<Map<String, Object>> writeMap() {
        List<Map<Column, Object>> table = $engine().query(this.$dql());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<Column, Object> map : table) {
            Map<String, Object> $map = new HashMap<>();
            result.add($map);
            map.forEach((k, v) -> $map.put(k.getAlias(), v));
        }
        return result;
    }


}
