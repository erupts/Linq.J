package xyz.erupt.linq.engine;

import xyz.erupt.linq.consts.JoinStrategy;
import xyz.erupt.linq.consts.OrderByDirection;
import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.It;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.Accessors;
import xyz.erupt.linq.util.Columns;
import xyz.erupt.linq.util.ReflectField;
import xyz.erupt.linq.util.RowUtil;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EruptEngine extends Engine {

    @Override
    public List<Row> query(Dql dql) {
        this.removeAmbiguousColumn(dql);
        // where pushdown: single-table value predicates run against the source objects, so
        // rejected elements never pay the Row materialization cost
        List<?> filteredSource = this.filterAtSource(dql);
        List<Row> dataset = this.toTable(null != filteredSource ? filteredSource : dql.getFrom());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            this.join(dql, dataset);
        }
        // where process - optimized to use iterator for better performance
        if (null == filteredSource && !dql.getWheres().isEmpty()) {
            List<WhereSchema> wheres = dql.getWheres();
            int whereCount = wheres.size();
            // Use array for faster access
            Predicate<Row>[] whereArray = new Predicate[whereCount];
            for (int i = 0; i < whereCount; i++) {
                whereArray[i] = wheres.get(i).getCondition();
            }
            dataset.removeIf(row -> {
                for (int i = 0; i < whereCount; i++) {
                    if (!whereArray[i].test(row)) return true;
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
            boolean rowsReused = false;
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
                        // Rows already carry the right columns — skip rebuilding them, but
                        // still fall through to having/orderBy/limit/distinct below.
                        rowsReused = true;
                    }
                }
            }

            // Normal select process - create new rows (skipped when rows were reused above)
            if (!rowsReused) {
                // Don't pre-allocate full capacity for very large datasets to avoid OOM
                // ArrayList will grow dynamically, which is acceptable for performance
                int datasetSize = dataset.size();
                List<Row> $table = new ArrayList<>(Math.min(datasetSize, 10000));
                Column[] columnsArray = columns.toArray(new Column[columnCount]);

                // Optimize: batch process to reduce overhead
                for (int rowIdx = 0; rowIdx < datasetSize; rowIdx++) {
                    Row row = dataset.get(rowIdx);
                    Row newRow = new Row(columnCount);
                    // Pre-check if we can optimize by avoiding null checks
                    // Use putDirect for performance - columns are added in order without duplicates
                    for (int i = 0; i < columnCount; i++) {
                        Column column = columnsArray[i];
                        if (hasGroupByFun[i]) {
                            existGroupFun = true;
                            newRow.putDirect(column, column.getGroupByFun().apply(dataset));
                        } else {
                            // Use pre-computed raw column - optimize branch prediction
                            Object value = row.get(rawColumns[i]);
                            if (hasRowConvert[i]) {
                                newRow.putDirect(column, column.getRowConvert().apply(row));
                            } else {
                                newRow.putDirect(column, value);
                            }
                        }
                    }
                    $table.add(newRow);
                    if (existGroupFun) break;
                }
                dataset.clear();
                dataset.addAll($table);
            }
        }
        // having process - optimized to use iterator for better performance
        if (!dql.getHaving().isEmpty()) {
            List<Predicate<Row>> having = dql.getHaving();
            int havingCount = having.size();
            // Use array for faster access
            Predicate<Row>[] havingArray = having.toArray(new Predicate[havingCount]);
            dataset.removeIf(row -> {
                for (int i = 0; i < havingCount; i++) {
                    if (!havingArray[i].test(row)) return true;
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

    // Materialize the source list into rows. Overridable so engines (e.g. a parallel one)
    // can change how the per-element read is executed.
    protected List<Row> toTable(List<?> from) {
        return RowUtil.listToTable(from);
    }

    /**
     * Where pushdown. When the query has no joins and every where clause carries a value
     * predicate (i.e. was declared through a typed {@code where(column, predicate)} variant),
     * the predicates are evaluated against the source objects directly and the surviving
     * subset is returned for materialization.
     *
     * <p>Matching mirrors the row path exactly: a typed where resolves its value by field
     * name, so the predicate reads the same field the materialized row would have carried.
     * Null elements are skipped — {@code listToTable} drops them anyway.
     *
     * @return the filtered source list, or {@code null} when pushdown does not apply and the
     * engine must fall back to row-level filtering.
     */
    @SuppressWarnings("unchecked")
    protected List<?> filterAtSource(Dql dql) {
        if (!dql.getJoinSchemas().isEmpty() || dql.getWheres().isEmpty()) return null;
        List<?> from = dql.getFrom();
        if (null == from || from.isEmpty()) return null;
        Object first = null;
        for (Object obj : from) {
            if (null != obj) {
                first = obj;
                break;
            }
        }
        if (null == first) return null;
        Class<?> clazz = first.getClass();
        boolean simpleClass = RowUtil.isSimpleClass(clazz);
        int whereCount = dql.getWheres().size();
        Function<Object, Object>[] extractors = new Function[whereCount];
        Predicate<Object>[] predicates = new Predicate[whereCount];
        for (int i = 0; i < whereCount; i++) {
            WhereSchema schema = dql.getWheres().get(i);
            if (null == schema.getValueCondition() || null == schema.getRelationColumn()) return null;
            predicates[i] = schema.getValueCondition();
            Column column = schema.getRelationColumn();
            if (simpleClass) {
                // simple sources have a single Th column whose value is the element itself
                if (It.class != column.getTable()) return null;
                extractors[i] = it -> it;
            } else {
                Field field = this.findField(clazz, column.getField());
                if (null == field) return null;
                extractors[i] = this.fieldReader(clazz, field);
            }
        }
        List<Object> filtered = new ArrayList<>();
        outer:
        for (Object obj : from) {
            if (null == obj) continue;
            for (int i = 0; i < whereCount; i++) {
                if (!predicates[i].test(extractors[i].apply(obj))) continue outer;
            }
            filtered.add(obj);
        }
        return filtered;
    }

    // ==================== direct path (object -> object, no Row) ====================

    /**
     * Single-table fast path: when no stage of the pipeline needs the Row intermediate
     * representation, transform source objects straight into target instances — the same
     * shape as a hand-written loop. Any condition it cannot prove returns {@code null},
     * and the caller falls back to the Row pipeline, so semantics never degrade.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> queryDirect(Dql dql, Class<T> target) {
        if (!this.directEligible(dql)) return null;
        List<?> from = dql.getFrom();
        if (null == from) return null;
        Object first = null;
        for (Object obj : from) {
            if (null != obj) {
                first = obj;
                break;
            }
        }
        if (null == first) return new ArrayList<>(0);
        List<Column> columns = this.uniqueColumns(dql);
        Function<Object, Object> mapper = this.directMapper(first.getClass(), columns, target);
        if (null == mapper) return null;
        List<?> source = from;
        if (!dql.getWheres().isEmpty()) {
            source = this.filterAtSource(dql);
            if (null == source) return null;
        }
        return (List<T>) this.executeDirect(dql, source, mapper);
    }

    @Override
    public List<Map<String, Object>> queryDirectMap(Dql dql) {
        if (!this.directEligible(dql)) return null;
        List<?> from = dql.getFrom();
        if (null == from) return null;
        Object first = null;
        for (Object obj : from) {
            if (null != obj) {
                first = obj;
                break;
            }
        }
        if (null == first) return new ArrayList<>(0);
        List<Column> columns = this.uniqueColumns(dql);
        int columnCount = columns.size();
        Function<Object, Object>[] extractors = this.directExtractors(first.getClass(), columns);
        if (null == extractors) return null;
        String[] aliases = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            aliases[i] = columns.get(i).getAlias();
        }
        List<?> source = from;
        if (!dql.getWheres().isEmpty()) {
            source = this.filterAtSource(dql);
            if (null == source) return null;
        }
        return this.executeDirect(dql, source, src -> {
            Map<String, Object> map = new HashMap<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                map.put(aliases[i], extractors[i].apply(src));
            }
            return map;
        });
    }

    // Direct path applies only when every skipped stage is provably a no-op.
    private boolean directEligible(Dql dql) {
        if (!dql.getJoinSchemas().isEmpty()) return false;
        if (!dql.getGroupBys().isEmpty() || !dql.getHaving().isEmpty()) return false;
        if (!dql.getOrderBys().isEmpty() || dql.isDistinct()) return false;
        for (Column column : dql.getColumns()) {
            if (null != column.getGroupByFun() || null != column.getRowConvert()) return false;
        }
        return true;
    }

    // Non-mutating equivalent of removeAmbiguousColumn: last column per alias wins.
    private List<Column> uniqueColumns(Dql dql) {
        Map<String, Column> unique = new LinkedHashMap<>();
        for (Column column : dql.getColumns()) {
            unique.put(column.getAlias(), column);
        }
        return new ArrayList<>(unique.values());
    }

    // Per-column source readers; null when any column cannot be read off the source class.
    @SuppressWarnings("unchecked")
    private Function<Object, Object>[] directExtractors(Class<?> sourceClazz, List<Column> columns) {
        boolean simpleSource = RowUtil.isSimpleClass(sourceClazz);
        Function<Object, Object>[] extractors = new Function[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (simpleSource) {
                // simple sources carry a single Th column whose value is the element itself
                if (It.class != column.getTable()) return null;
                extractors[i] = it -> it;
            } else {
                Field field = this.findField(sourceClazz, column.getField());
                if (null == field) return null;
                extractors[i] = this.fieldReader(sourceClazz, field);
            }
        }
        return extractors;
    }

    /**
     * Compile the per-element transform. Two shapes:
     * <ul>
     * <li>cast mode — single column whose value passes through as the result element
     * (mirrors the single-column fast path in {@code Write#write});</li>
     * <li>pojo mode — instantiate the target and write each aliased column through an
     * {@link Accessors} setter (reflection fallback), mirroring {@code RowUtil#rowToObject}.</li>
     * </ul>
     */
    private Function<Object, Object> directMapper(Class<?> sourceClazz, List<Column> columns, Class<?> target) {
        Function<Object, Object>[] extractors = this.directExtractors(sourceClazz, columns);
        if (null == extractors) return null;
        boolean simpleSource = RowUtil.isSimpleClass(sourceClazz);
        int columnCount = columns.size();
        // cast mode: the single value is the element
        if (columnCount == 1) {
            Class<?> valueType = simpleSource ? sourceClazz : wrap(this.findField(sourceClazz, columns.get(0).getField()).getType());
            if (target.isAssignableFrom(valueType) || RowUtil.isSimpleClass(target)) {
                boolean convertBig = !target.isAssignableFrom(BigDecimal.class);
                Function<Object, Object> extractor = extractors[0];
                return src -> {
                    Object value = extractor.apply(src);
                    if (convertBig && value instanceof BigDecimal) {
                        return RowUtil.bigDecimalConvert((BigDecimal) value, target);
                    }
                    return value;
                };
            }
        }
        if (simpleSource) return null;
        // pojo mode
        Constructor<?> ctor;
        try {
            ctor = target.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (!ctor.isAccessible()) ctor.setAccessible(true);
        Function<Object, Object>[] reads = new Function[columnCount];
        BiConsumer<Object, Object>[] writes = new BiConsumer[columnCount];
        Class<?>[] fieldTypes = new Class[columnCount];
        int matched = 0;
        for (int i = 0; i < columnCount; i++) {
            Field targetField = this.findField(target, columns.get(i).getAlias());
            if (null == targetField) continue; // rowToObject ignores aliases without a target field
            reads[matched] = extractors[i];
            fieldTypes[matched] = targetField.getType();
            writes[matched] = this.fieldWriter(target, targetField);
            matched++;
        }
        // a single unmatched column may hit Write's runtime cast fast path — let the Row path decide
        if (matched == 0 && columnCount == 1) return null;
        int count = matched;
        return src -> {
            try {
                Object out = ctor.newInstance();
                for (int i = 0; i < count; i++) {
                    Object value = reads[i].apply(src);
                    if (null == value) continue; // row materialization omits null values
                    if (value instanceof BigDecimal) {
                        value = RowUtil.bigDecimalConvert((BigDecimal) value, fieldTypes[i]);
                    }
                    writes[i].accept(out, value);
                }
                return out;
            } catch (Exception e) {
                throw new LinqException(e);
            }
        };
    }

    private <T> List<T> executeDirect(Dql dql, List<?> source, Function<Object, T> mapper) {
        int offset = null == dql.getOffset() ? 0 : dql.getOffset();
        Integer limit = dql.getLimit();
        if (0 == offset && null == limit) {
            return this.mapSource(source, mapper);
        }
        List<T> result = new ArrayList<>();
        int skipped = 0;
        for (Object src : source) {
            if (null == src) continue;
            if (skipped < offset) {
                skipped++;
                continue;
            }
            if (null != limit && result.size() >= limit) break;
            result.add(mapper.apply(src));
        }
        return result;
    }

    // Bulk transform hook. Overridable so engines (e.g. the parallel one) can change execution.
    protected <T> List<T> mapSource(List<?> source, Function<Object, T> mapper) {
        List<T> result = new ArrayList<>(Math.min(source.size(), 10000));
        for (Object src : source) {
            if (null != src) result.add(mapper.apply(src));
        }
        return result;
    }

    protected Field findField(Class<?> clazz, String name) {
        for (Field field : ReflectField.getFields(clazz)) {
            if (field.getName().equals(name)) return field;
        }
        return null;
    }

    protected Function<Object, Object> fieldReader(Class<?> clazz, Field field) {
        Function<Object, Object> getter = Accessors.getter(clazz, field.getName(), field.getType());
        if (null != getter) return getter;
        if (!field.isAccessible()) field.setAccessible(true);
        return it -> {
            try {
                return field.get(it);
            } catch (IllegalAccessException e) {
                throw new LinqException(e);
            }
        };
    }

    protected BiConsumer<Object, Object> fieldWriter(Class<?> clazz, Field field) {
        BiConsumer<Object, Object> setter = Accessors.setter(clazz, field.getName(), field.getType());
        if (null != setter) return setter;
        if (!field.isAccessible()) field.setAccessible(true);
        return (obj, value) -> {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new LinqException(e);
            }
        };
    }

    private static Class<?> wrap(Class<?> type) {
        return type.isPrimitive() ? MethodType.methodType(type).wrap().returnType() : type;
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
            Column lon = Columns.of(joinSchema.getTargetOn());
            Column ron = Columns.of(joinSchema.getSourceOn());
            if (joinSchema.getJoinStrategy() == JoinStrategy.HASH) {
                List<Row> targetData = RowUtil.listToTable(joinSchema.getTarget());
                switch (joinSchema.getJoinType()) {
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
                throw new LinqException(joinSchema.getJoinStrategy().name() + " is not supported yet");
            }
        }
    }

    //Cartesian product case
    private void crossHashJoin(List<Row> source, Column sourceColumn,
                               List<Row> target, Column targetColumn) {
        // Build side: one get() per row, ArrayList buckets (LinkedList wastes a node per element).
        Map<Object, List<Row>> rightMap = new HashMap<>();
        for (Row row : target) {
            rightMap.computeIfAbsent(row.get(targetColumn), k -> new ArrayList<>()).add(row);
        }
        // Probe side: resolve the bucket once per row instead of re-reading the key each access.
        ListIterator<Row> iterator = source.listIterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            List<Row> matches = rightMap.get(row.get(sourceColumn));
            if (null != matches) {
                for (int i = matches.size() - 1; i >= 0; i--) {
                    if (i == 0) {
                        row.putAll(matches.get(i));
                    } else {
                        Row cartesianRow = new Row(row);
                        cartesianRow.putAll(matches.get(i));
                        iterator.add(cartesianRow);
                    }
                }
            }
        }
    }

    public List<Row> groupBy(Dql dql, List<Row> dataset) {
        List<Column> groupBys = dql.getGroupBys();
        int gCount = groupBys.size();
        // Pre-resolve once: getRawColumn() allocates a Column, so never call it per row.
        Column[] groupRaw = new Column[gCount];
        Function<Row, ?>[] groupConvert = new Function[gCount];
        for (int i = 0; i < gCount; i++) {
            Column g = groupBys.get(i);
            groupConvert[i] = g.getRowConvert(); // getRawColumn() only copies this reference
            groupRaw[i] = g.getRawColumn();
        }
        // Single group key uses the value itself; multi-key joins with a separator that cannot
        // collide across columns (the old no-separator concatenation could merge distinct groups).
        Map<Object, List<Row>> groupMap = new LinkedHashMap<>();
        for (Row row : dataset) {
            Object key;
            if (gCount == 1) {
                key = null != groupConvert[0] ? groupConvert[0].apply(row) : row.get(groupRaw[0]);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < gCount; i++) {
                    sb.append(null != groupConvert[i] ? groupConvert[i].apply(row) : row.get(groupRaw[i]));
                    sb.append('');
                }
                key = sb.toString();
            }
            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        // Pre-resolve select columns / aggregate fns once, outside the group loop.
        List<Column> cols = dql.getColumns();
        int cCount = cols.size();
        Column[] rawCols = new Column[cCount];
        @SuppressWarnings("unchecked")
        Function<List<Row>, Object>[] groupFuns = new Function[cCount];
        for (int i = 0; i < cCount; i++) {
            Column c = cols.get(i);
            groupFuns[i] = c.getGroupByFun(); // getRawColumn() only copies this reference
            rawCols[i] = c.getRawColumn();
        }
        List<Row> result = new ArrayList<>(groupMap.size());
        for (List<Row> group : groupMap.values()) {
            Row values = new Row(cCount);
            result.add(values);
            Row firstRow = group.isEmpty() ? null : group.get(0);
            for (int i = 0; i < cCount; i++) {
                Object val;
                if (null != groupFuns[i]) {
                    val = groupFuns[i].apply(group);
                } else {
                    val = null != firstRow ? firstRow.get(rawCols[i]) : null;
                }
                values.putDirect(cols.get(i), val);
            }
        }
        return result;
    }

    public void orderBy(Dql dql, List<Row> dataset) {
        List<OrderBySchema> orderBys = dql.getOrderBys();
        int keyCount = orderBys.size();
        Column[] cols = new Column[keyCount];
        boolean[] desc = new boolean[keyCount];
        for (int i = 0; i < keyCount; i++) {
            cols[i] = orderBys.get(i).getColumn();
            desc[i] = orderBys.get(i).getDirection() == OrderByDirection.DESC;
        }
        // Comparator reads pre-resolved column/direction arrays (no per-compare iterator) and
        // does one get() per side per key instead of the previous six. Decorate-sort was tried
        // and lost: boxing an Integer index per row cost more than the get() it saved on narrow
        // rows, where get() is an O(1-2) linear scan anyway.
        dataset.sort((a, b) -> {
            for (int c = 0; c < keyCount; c++) {
                Object av = a.get(cols[c]);
                Object bv = b.get(cols[c]);
                if (null == av || null == bv) return 0; // preserve legacy null-stops-compare semantics
                if (!(av instanceof Comparable)) {
                    throw new LinqException(cols[c].getTable() + "." + cols[c].getField() + " sort does not implement the Comparable interface");
                }
                @SuppressWarnings("unchecked")
                int cmp = ((Comparable<Object>) av).compareTo(bv);
                if (desc[c]) cmp = -cmp;
                if (cmp != 0) return cmp;
            }
            return 0;
        });
    }

}
