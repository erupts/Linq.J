package xyz.erupt.linq.engine;

import xyz.erupt.linq.consts.JoinExchange;
import xyz.erupt.linq.consts.OrderByDirection;
import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.Columns;
import xyz.erupt.linq.util.RowUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EruptEngine extends Engine {

    @Override
    public List<Row> query(Dql dql) {
        this.removeAmbiguousColumn(dql);
        List<Row> dataset = RowUtil.listToTable(dql.getFrom());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            this.join(dql, dataset);
        }
        // where process - optimized to use iterator for better performance
        if (!dql.getWheres().isEmpty()) {
            List<Function<Row, Boolean>> wheres = dql.getWheres();
            int whereCount = wheres.size();
            // Use array for faster access
            Function<Row, Boolean>[] whereArray = wheres.toArray(new Function[whereCount]);
            dataset.removeIf(row -> {
                for (int i = 0; i < whereCount; i++) {
                    if (!whereArray[i].apply(row)) return true;
                }
                return false;
            });
        }
        // group by process
        if (null != dql.getGroupBys() && !dql.getGroupBys().isEmpty()) {
            dataset = this.groupBy(dql, dataset);
        } else {
            // simple select process - optimized to reduce object creation
            List<Column> columns = dql.getColumns();
            int columnCount = columns.size();

            // Check if we can reuse existing rows (no rowConvert, no groupByFun, columns match)
            boolean canReuseRows = true;
            boolean existGroupFun = false;
            Column[] rawColumns = new Column[columnCount];
            boolean[] hasRowConvert = new boolean[columnCount];
            boolean[] hasGroupByFun = new boolean[columnCount];

            for (int i = 0; i < columnCount; i++) {
                Column col = columns.get(i);
                rawColumns[i] = col.getRawColumn();
                hasRowConvert[i] = col.getRowConvert() != null;
                hasGroupByFun[i] = col.getGroupByFun() != null;
                if (hasRowConvert[i] || hasGroupByFun[i]) {
                    canReuseRows = false;
                }
                if (hasGroupByFun[i]) {
                    existGroupFun = true;
                }
            }

            // Optimization: if columns match exactly and no conversion needed, reuse rows
            if (canReuseRows && !dataset.isEmpty()) {
                Row firstRow = dataset.get(0);
                int firstRowSize = firstRow.size();
                if (firstRowSize == columnCount) {
                    // Check if all selected columns exist in original row
                    boolean allMatch = true;
                    for (int i = 0; i < columnCount; i++) {
                        Column rawCol = rawColumns[i];
                        if (!firstRow.containsKey(rawCol)) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (allMatch) {
                        // Just update column aliases if needed, no need to create new rows
                        // Check if aliases need to be updated
                        boolean needUpdateAlias = false;
                        for (int i = 0; i < columnCount; i++) {
                            Column rawCol = rawColumns[i];
                            Column targetCol = columns.get(i);
                            if (!rawCol.getAlias().equals(targetCol.getAlias())) {
                                needUpdateAlias = true;
                                break;
                            }
                        }
                        if (needUpdateAlias) {
                            // Update aliases in place
                            for (Row row : dataset) {
                                for (int i = 0; i < columnCount; i++) {
                                    Column rawCol = rawColumns[i];
                                    Column targetCol = columns.get(i);
                                    if (rawCol != targetCol && row.containsKey(rawCol)) {
                                        Object value = row.get(rawCol);
                                        row.put(targetCol, value);
                                        row.remove(rawCol);
                                    }
                                }
                            }
                        }
                        // Skip the rest of select processing - rows are already correct
                        return dataset;
                    }
                }
            }

            // Normal select process - create new rows
            List<Row> $table = new ArrayList<>(dataset.size());
            Column[] columnsArray = columns.toArray(new Column[columnCount]);
            int datasetSize = dataset.size();

            // Optimize: batch process to reduce overhead
            for (int rowIdx = 0; rowIdx < datasetSize; rowIdx++) {
                Row row = dataset.get(rowIdx);
                Row newRow = new Row(columnCount);
                // Pre-check if we can optimize by avoiding null checks
                for (int i = 0; i < columnCount; i++) {
                    Column column = columnsArray[i];
                    if (hasGroupByFun[i]) {
                        existGroupFun = true;
                        newRow.put(column, column.getGroupByFun().apply(dataset));
                    } else {
                        // Use pre-computed raw column - optimize branch prediction
                        Object value = row.get(rawColumns[i]);
                        if (hasRowConvert[i]) {
                            newRow.put(column, column.getRowConvert().apply(row));
                        } else {
                            newRow.put(column, value);
                        }
                    }
                }
                $table.add(newRow);
                if (existGroupFun) break;
            }
            dataset.clear();
            dataset.addAll($table);
        }
        // having process - optimized to use iterator for better performance
        if (!dql.getHaving().isEmpty()) {
            List<Function<Row, Boolean>> having = dql.getHaving();
            int havingCount = having.size();
            // Use array for faster access
            Function<Row, Boolean>[] havingArray = having.toArray(new Function[havingCount]);
            dataset.removeIf(row -> {
                for (int i = 0; i < havingCount; i++) {
                    if (!havingArray[i].apply(row)) return true;
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

    // remove ambiguous column
    private void removeAmbiguousColumn(Dql dql) {
        Map<String, Column> uniqueColumns = new HashMap<>();
        for (Column column : dql.getColumns()) {
            uniqueColumns.put(column.getAlias(), column);
        }
        dql.getColumns().clear();
        dql.getColumns().addAll(uniqueColumns.values());
    }

    // Note: The original code used removeAll which would remove all duplicate entries.
    // This modified code uses a HashMap to keep only the first occurrence of each alias,
    // and then sets the columns of the Dql object to the values of this HashMap.
    public void join(Dql dql, List<Row> dataset) {
        for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
            Column lon = Columns.of(joinSchema.getLon());
            Column ron = Columns.of(joinSchema.getRon());
            if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                List<Row> targetData = RowUtil.listToTable(joinSchema.getTarget());
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
