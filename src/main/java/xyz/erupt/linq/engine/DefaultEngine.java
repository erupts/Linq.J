package xyz.erupt.linq.engine;

import xyz.erupt.linq.consts.JoinExchange;
import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.grammar.OrderBy;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.ColumnReflects;
import xyz.erupt.linq.util.Columns;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultEngine extends Engine {

    @Override
    public List<Row> query(Dql dql) {
        List<Row> table = ColumnReflects.listToRow(dql.getSource());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
                Column lon = Columns.fromLambda(joinSchema.getLon());
                Column ron = Columns.fromLambda(joinSchema.getRon());
                if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                    List<Row> targetData = ColumnReflects.listToRow(joinSchema.getTarget());
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
                    throw new LinqException(joinSchema.getJoinExchange().name() + " is not supported yet");
                }
            }
        }
        // condition process
        table.removeIf(it -> {
            for (Function<Row, Boolean> condition : dql.getConditions()) {
                if (!condition.apply(it)) return true;
            }
            return false;
        });
        // group by process
        if (null != dql.getGroupBys() && !dql.getGroupBys().isEmpty()) {
            Map<String, List<Row>> groupMap = new HashMap<>();
            for (Row row : table) {
                StringBuilder key = new StringBuilder();
                for (Column groupBy : dql.getGroupBys()) {
                    if (null != groupBy.getValueConvertFun()) {
                        key.append(groupBy.getValueConvertFun().apply(row));
                    } else {
                        key.append(row.get(groupBy));
                    }
                }
                if (!groupMap.containsKey(key.toString())) {
                    groupMap.put(key.toString(), new ArrayList<>());
                }
                groupMap.get(key.toString()).add(row);
            }
            table = new ArrayList<>(groupMap.size());
            // group by select process
            for (Map.Entry<String, List<Row>> entry : groupMap.entrySet()) {
                Row values = new Row(dql.getColumns().size());
                table.add(values);
                for (Column column : dql.getColumns()) {
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
            List<Row> $table = new ArrayList<>(table.size());
            boolean existGroupFun = false;
            for (Row row : table) {
                Row newRow = new Row(dql.getColumns().size());
                for (Column column : dql.getColumns()) {
                    if (null != column.getGroupByFun()) {
                        existGroupFun = true;
                        newRow.put(column, column.getGroupByFun().apply(table));
                    } else {
                        newRow.put(column, row.get(column.getRawColumn()));
                    }
                    if (null != column.getValueConvertFun()) {
                        newRow.put(column, column.getValueConvertFun().apply(row));
                    }
                }
                $table.add(newRow);
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
    private void crossHashJoin(List<Row> source, Column sourceColumn,
                               List<Row> target, Column targetColumn) {
        Map<Object, List<Row>> rightMap = new HashMap<>();
        for (Row row : target) {
            if (!rightMap.containsKey(row.get(targetColumn))) {
                rightMap.put(row.get(targetColumn), new LinkedList<>());
            }
            rightMap.get(row.get(targetColumn)).add(row);
        }
        ListIterator<Row> iterator = source.listIterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (rightMap.containsKey(row.get(sourceColumn))) {
                for (int i = rightMap.get(row.get(sourceColumn)).size() - 1; i >= 0; i--) {
                    if (i == 0) {
                        row.putAll(rightMap.get(row.get(sourceColumn)).get(i));
                    } else {
                        Row cartesianRow = new Row(row);
                        cartesianRow.putAll(rightMap.get(row.get(sourceColumn)).get(i));
                        iterator.add(cartesianRow);
                    }
                }
            }
        }
    }

    private void orderBy(Dql dql, List<Row> dataset) {
        if (null != dql.getOrderBys() && !dql.getOrderBys().isEmpty()) {
            dataset.sort((a, b) -> {
                int i = 0;
                if (a == null || b == null) {
                    return i;
                }
                for (OrderByColumn orderBy : dql.getOrderBys()) {
                    if (null == a.get(orderBy.getColumn())) return 0;
                    if (a.get(orderBy.getColumn()) instanceof Comparable) {
                        Comparable<Object> comparable = (Comparable<Object>) a.get(orderBy.getColumn());
                        i = comparable.compareTo(b.get(orderBy.getColumn()));
                        if (orderBy.getDirection() == OrderBy.Direction.DESC) i = ~i + 1;
                        if (i != 0) return i;
                    } else {
                        throw new LinqException(orderBy.getColumn().getTable() + "." + orderBy.getColumn().getField() + " sort does not implement the Comparable interface");
                    }
                }
                return i;
            });
        }
    }

}
