package xyz.erupt.eql.engine;

import xyz.erupt.eql.consts.JoinExchange;
import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.grammar.OrderBy;
import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;
import xyz.erupt.eql.schema.OrderByColumn;
import xyz.erupt.eql.util.Columns;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultEngine extends Engine {

    @Override
    public List<Map<Column<?>, Object>> query(Dql dql) {
        List<Map<Column<?>, Object>> table = LambdaInfo.objectToLambdaInfos(dql.getSource());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
                Column<?> lon = Columns.fromLambda(joinSchema.getLon());
                Column<?> ron = Columns.fromLambda(joinSchema.getRon());
                if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                    List<Map<Column<?>, Object>> targetData = LambdaInfo.objectToLambdaInfos(joinSchema.getTarget());
                    switch (joinSchema.getJoinMethod()) {
                        case LEFT:
                            this.crossHashJoin(table, ron, targetData, lon);
                            break;
                        case RIGHT:
                            this.crossHashJoin(targetData, lon, table, ron);
                            table = targetData;
                            break;
                        case INNER:
                            this.crossHashJoin(table, ron, targetData, lon);
                            table.removeIf(it -> !it.containsKey(lon));
                            break;
                        case FULL:
                            this.crossHashJoin(table, ron, targetData, lon);
                            this.crossHashJoin(targetData, lon, table, ron);
                            targetData.removeIf(it -> it.containsKey(ron));
                            table.addAll(targetData);
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
            table = new ArrayList<>(groupMap.size());
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
                Map<Column<?>, Object> map = new HashMap<>(dql.getColumns().size());
                for (Column<?> column : dql.getColumns()) {
                    if (null != column.getRawColumn().getGroupByFun()) {
                        existGroupFun = true;
                        map.put(column, column.getGroupByFun().apply(table));
                    } else {
                        map.put(column, data.get(column.getRawColumn()));
                    }
                }
                $table.add(map);
                if (existGroupFun) break;
            }
            table.clear();
            table.addAll($table);
        }
        // order by process
        this.orderBy(dql, table);
        // limit
        if (null != dql.getOffset()) {
            table = dql.getOffset() > table.size() ? new ArrayList<>(0) : table.subList(dql.getOffset(), table.size());
        }
        if (null != dql.getLimit()) {
            table = table.subList(0, dql.getLimit() > table.size() ? table.size() : dql.getLimit());
        }
        if (dql.isDistinct()) {
            table = table.stream().distinct().collect(Collectors.toList());
        }
        return table;
    }

    //Cartesian product case
    private void crossHashJoin(List<Map<Column<?>, Object>> source, Column<?> sourceColumn,
                               List<Map<Column<?>, Object>> target, Column<?> targetColumn) {
        Map<Object, List<Map<Column<?>, ?>>> rightMap = new HashMap<>();
        for (Map<Column<?>, Object> objectMap : target) {
            if (!rightMap.containsKey(objectMap.get(targetColumn))) {
                rightMap.put(objectMap.get(targetColumn), new LinkedList<>());
            }
            rightMap.get(objectMap.get(targetColumn)).add(objectMap);
        }
        ListIterator<Map<Column<?>, Object>> iterator = source.listIterator();
        while (iterator.hasNext()) {
            Map<Column<?>, Object> map = iterator.next();
            if (rightMap.containsKey(map.get(sourceColumn))) {
                for (int i = rightMap.get(map.get(sourceColumn)).size() - 1; i >= 0; i--) {
                    if (i == 0) {
                        map.putAll(rightMap.get(map.get(sourceColumn)).get(i));
                    } else {
                        Map<Column<?>, Object> cartesianMap = new HashMap<>(map);
                        cartesianMap.putAll(rightMap.get(map.get(sourceColumn)).get(i));
                        iterator.add(cartesianMap);
                    }
                }
            }
        }
    }

    private void orderBy(Dql dql, List<Map<Column<?>, Object>> dataset) {
        if (null != dql.getOrderBys() && !dql.getOrderBys().isEmpty()) {
            dataset.sort((a, b) -> {
                int i = 0;
                for (OrderByColumn orderBy : dql.getOrderBys()) {
                    if (null == a.get(orderBy.getColumn())) {
                        throw new EqlException("Unknown column '" + orderBy.getColumn().getTable() + "." + orderBy.getColumn().getField() + "' in 'order clause'");
                    }
                    if (a.get(orderBy.getColumn()) instanceof Comparable) {
                        Comparable<Object> comparable = (Comparable<Object>) a.get(orderBy.getColumn());
                        i = comparable.compareTo(b.get(orderBy.getColumn()));
                        if (orderBy.getDirection() == OrderBy.Direction.DESC) i = ~i + 1;
                        if (i != 0) return i;
                    } else {
                        throw new EqlException(orderBy.getColumn().getTable() + "." + orderBy.getColumn().getField() + " sort does not implement the Comparable interface");
                    }
                }
                return i;
            });
        }
    }

}
