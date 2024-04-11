package xyz.erupt.linq.engine;

import xyz.erupt.linq.consts.JoinExchange;
import xyz.erupt.linq.consts.OrderByDirection;
import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.RowUtil;
import xyz.erupt.linq.util.Columns;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EruptEngine extends Engine {

    @Override
    public List<Row> query(Dql dql) {
        List<Row> dataset = RowUtil.listObjectToRow(dql.getFrom());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            this.join(dql, dataset);
        }
        // where process
        if (!dql.getWheres().isEmpty()) {
            dataset.removeIf(it -> {
                for (Function<Row, Boolean> condition : dql.getWheres()) {
                    if (!condition.apply(it)) return true;
                }
                return false;
            });
        }
        // group by process
        if (null != dql.getGroupBys() && !dql.getGroupBys().isEmpty()) {
            dataset = this.groupBy(dql, dataset);
        } else {
            // simple select process
            List<Row> $table = new ArrayList<>(dataset.size());
            boolean existGroupFun = false;
            for (Row row : dataset) {
                Row newRow = new Row(dql.getColumns().size());
                for (Column column : dql.getColumns()) {
                    if (null != column.getGroupByFun()) {
                        existGroupFun = true;
                        newRow.put(column, column.getGroupByFun().apply(dataset));
                    } else {
                        newRow.put(column, row.get(column.getRawColumn()));
                    }
                    if (null != column.getRowConvert()) {
                        newRow.put(column, column.getRowConvert().apply(row));
                    }
                }
                $table.add(newRow);
                if (existGroupFun) break;
            }
            dataset.clear();
            dataset.addAll($table);
        }
        // having process
        if (!dql.getHaving().isEmpty()) {
            dataset.removeIf(it -> {
                for (Function<Row, Boolean> condition : dql.getHaving()) {
                    if (!condition.apply(it)) return true;
                }
                return false;
            });
        }
        // order by process
        if (!dql.getOrderBys().isEmpty()) {
            this.orderBy(dql, dataset);
        }
        // limit
        if (null != dql.getOffset()) {
            dataset = dql.getOffset() > dataset.size() ? new ArrayList<>(0) : dataset.subList(dql.getOffset(), dataset.size());
        }
        if (null != dql.getLimit()) {
            dataset = dataset.subList(0, dql.getLimit() > dataset.size() ? dataset.size() : dql.getLimit());
        }
        // distinct process
        if (dql.isDistinct()) {
            dataset = dataset.stream().distinct().collect(Collectors.toList());
        }
        return dataset;
    }

    public void join(Dql dql, List<Row> dataset) {
        for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
            Column lon = Columns.of(joinSchema.getLon());
            Column ron = Columns.of(joinSchema.getRon());
            if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                List<Row> targetData = RowUtil.listObjectToRow(joinSchema.getTarget());
                switch (joinSchema.getJoinMethod()) {
                    case LEFT:
                        this.crossHashJoin(dataset, ron, targetData, lon);
                        break;
                    case RIGHT:
                        this.crossHashJoin(targetData, lon, dataset, ron);
                        dataset.clear();
                        dataset.addAll(targetData);
                        break;
                    case INNER:
                        this.crossHashJoin(dataset, ron, targetData, lon);
                        dataset.removeIf(it -> !it.containsKey(lon));
                        break;
                    case FULL:
                        this.crossHashJoin(dataset, ron, targetData, lon);
                        this.crossHashJoin(targetData, lon, dataset, ron);
                        targetData.removeIf(it -> it.containsKey(ron));
                        dataset.addAll(targetData);
                        break;
                }
            } else {
                throw new LinqException(joinSchema.getJoinExchange().name() + " is not supported yet");
            }
        }
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

    public List<Row> groupBy(Dql dql, List<Row> dataset) {
        Map<String, List<Row>> groupMap = new HashMap<>();
        for (Row row : dataset) {
            StringBuilder key = new StringBuilder();
            for (Column groupBy : dql.getGroupBys()) {
                if (null != groupBy.getRowConvert()) {
                    key.append(groupBy.getRawColumn().getRowConvert().apply(row));
                } else {
                    key.append(row.get(groupBy.getRawColumn()));
                }
            }
            if (!groupMap.containsKey(key.toString())) {
                groupMap.put(key.toString(), new ArrayList<>());
            }
            groupMap.get(key.toString()).add(row);
        }
        List<Row> result = new ArrayList<>(groupMap.size());
        // group by select process
        for (Map.Entry<String, List<Row>> entry : groupMap.entrySet()) {
            Row values = new Row(dql.getColumns().size());
            result.add(values);
            for (Column column : dql.getColumns()) {
                Object val = null;
                if (null != column.getGroupByFun()) {
                    val = column.getRawColumn().getGroupByFun().apply(entry.getValue());
                } else {
                    if (!entry.getValue().isEmpty()) {
                        val = entry.getValue().get(0).get(column.getRawColumn());
                    }
                }
                values.put(column, val);
            }
        }
        return result;
    }

    public void orderBy(Dql dql, List<Row> dataset) {
        dataset.sort((a, b) -> {
            int i = 0;
            for (OrderBySchema orderBy : dql.getOrderBys()) {
                if (null == a.get(orderBy.getColumn()) || null == b.get(orderBy.getColumn())) return 0;
                if (a.get(orderBy.getColumn()) instanceof Comparable) {
                    Comparable<Object> comparable = (Comparable<Object>) a.get(orderBy.getColumn());
                    i = comparable.compareTo(b.get(orderBy.getColumn()));
                    if (orderBy.getDirection() == OrderByDirection.DESC) i = ~i + 1;
                    if (i != 0) return i;
                } else {
                    throw new LinqException(orderBy.getColumn().getTable() + "." + orderBy.getColumn().getField() + " sort does not implement the Comparable interface");
                }
            }
            return i;
        });
    }

}
