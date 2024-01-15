package xyz.erupt.eql.query;

import xyz.erupt.eql.consts.JoinExchange;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.grammar.OrderBy;
import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;
import xyz.erupt.eql.schema.OrderByColumn;
import xyz.erupt.eql.util.Columns;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultQuery extends Query {

    @Override
    public <T> List<T> dql(Dql dql, Class<T> target) {
        List<Map<Column<?>, Object>> table = LambdaInfo.objectToLambdaInfos(dql.getSource());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
                Column<?> lon = Columns.fromLambda(joinSchema.getLon());
                Column<?> ron = Columns.fromLambda(joinSchema.getRon());
                if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                    switch (joinSchema.getJoinMethod()) {
                        case LEFT:
                        case INNER:
                        case FULL:
                            List<Map<Column<?>, Object>> targetData = LambdaInfo.objectToLambdaInfos(joinSchema.getTarget());
                            Map<Object, Map<Column<?>, ?>> rightMap = targetData.stream().collect(Collectors.toMap(map -> map.get(lon), map -> map));
                            for (Map<Column<?>, Object> map : table) {
                                if (rightMap.containsKey(map.get(ron))) map.putAll(rightMap.get(map.get(ron)));
                            }
                            if (JoinMethod.INNER == joinSchema.getJoinMethod())
                                table.removeIf(it -> !it.containsKey(lon));
                            if (JoinMethod.FULL == joinSchema.getJoinMethod()) {
                                Map<Object, Map<Column<?>, ?>> leftMap = table.stream().collect(Collectors.toMap(map -> map.get(ron), map -> map));
                                for (Map<Column<?>, Object> tmap : targetData) {
                                    if (!leftMap.containsKey(tmap.get(lon))) table.add(tmap);
                                }
                            }
                            break;
                        case RIGHT:
                            Map<Object, Map<Column<?>, ?>> leftMap = table.stream().collect(Collectors.toMap(map -> map.get(ron), map -> map));
                            table.clear();
                            for (Map<Column<?>, Object> map : LambdaInfo.objectToLambdaInfos(joinSchema.getTarget())) {
                                table.add(map);
                                if (leftMap.containsKey(map.get(lon))) map.putAll(leftMap.get(map.get(lon)));
                            }
                            break;
                    }
                } else {
                    throw new EqlException(joinSchema.getJoinExchange().name() + " is not supported yet");
                }
            }
        }
        // condition process
        table.removeIf(it -> {
            for (Function<Map<Column<?>, ?>, Boolean> condition : dql.getConditions()) {
                if (!condition.apply(it)) return true;
            }
            return false;
        });
        // group by process
        if (null != dql.getGroupBys() && !dql.getGroupBys().isEmpty()) {
            Map<String, List<Map<Column<?>, Object>>> groupMap = new HashMap<>();
            for (Map<Column<?>, Object> columns : table) {
                StringBuilder key = new StringBuilder();
                for (Column<?> groupBy : dql.getGroupBys()) {
                    if (null != groupBy.getValueConvertFun()) {
                        key.append(groupBy.getValueConvertFun().apply(columns.get(groupBy)));
                    } else {
                        key.append(columns.get(groupBy));
                    }
                }
                if (!groupMap.containsKey(key.toString())) {
                    groupMap.put(key.toString(), new ArrayList<>());
                }
                groupMap.get(key.toString()).add(columns);
            }
            table.clear();
            // group by select process
            for (Map.Entry<String, List<Map<Column<?>, Object>>> entry : groupMap.entrySet()) {
                Map<Column<?>, Object> values = new HashMap<>(dql.getColumns().size());
                table.add(values);
                for (Column<?> column : dql.getColumns()) {
                    Object val = null;
                    if (null != column.getGroupByFun()) {
                        val = column.getGroupByFun().apply(entry.getValue());
                    } else {
                        if (!entry.getValue().isEmpty()) {
                            val = entry.getValue().get(0).get(column.getRawColumn());
                        }
                    }
                    values.put(column, val);
                }
            }
        } else {
            // simple select process
            List<Map<Column<?>, Object>> $table = new ArrayList<>(table.size());
            boolean existGroupFun = false;
            for (Map<Column<?>, ?> data : table) {
                Map<Column<?>, Object> $map = new HashMap<>(dql.getColumns().size());
                for (Column<?> column : dql.getColumns()) {
                    if (null != column.getRawColumn().getGroupByFun()) {
                        existGroupFun = true;
                        $map.put(column, column.getGroupByFun().apply(table));
                    } else {
                        $map.put(column, data.get(column.getRawColumn()));
                    }
                }
                $table.add($map);
                if (existGroupFun) break;
            }
            table.clear();
            table.addAll($table);
        }

        // order by process
        if (null != dql.getOrderBys() && !dql.getOrderBys().isEmpty()) {
            table.sort((a, b) -> {
                int i = 0;
                for (OrderByColumn orderBy : dql.getOrderBys()) {
                    if (a.get(orderBy.getColumn()) instanceof Comparable) {
                        Comparable<Object> comparable = (Comparable<Object>) a.get(orderBy.getColumn());
                        i = comparable.compareTo(b.get(orderBy.getColumn()));
                        if (orderBy.getDirection() == OrderBy.Direction.DESC) {
                            i = ~i + 1;
                        }
                        if (i != 0) return i;
                    }
//                    else {
//                        throw new EqlException(orderBy.getColumn().getTable() + "." + orderBy.getColumn().getField() + " sort does not implement the Comparable interface");
//                    }
                }
                return i;
            });
        }
        // limit
        if (null != dql.getOffset()) {
            table = dql.getOffset() > table.size() ? new ArrayList<>(0) : table.subList(dql.getOffset(), table.size());
        }
        if (null != dql.getLimit()) {
            table = table.subList(0, dql.getLimit() > table.size() ? table.size() : dql.getLimit());
        }
        // result mapping
        List<T> result = null;
        if (target.getName().equals(Map.class.getName())) {
            result = new ArrayList<>();
            Map<String, Object> $map = new HashMap<>();
            table.forEach(map -> map.forEach((k, v) -> $map.put(k.getAlias(), v)));
            result.add((T) $map);
        } else {
            result = table.stream().map(it -> convertMapToObject(it, target)).collect(Collectors.toList());
        }
        // distinct process
        if (dql.isDistinct()) {
            result = result.stream().distinct().collect(Collectors.toList());
        }
        return result;
    }

    private static <T> T convertMapToObject(Map<Column<?>, Object> map, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<Column<?>, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey().getAlias());
                    field.setAccessible(true);
                    field.set(instance, entry.getValue());
                } catch (NoSuchFieldException ignore) {
                }
            }
            return instance;
        } catch (Exception e) {
            throw new EqlException(e);
        }
    }

}
