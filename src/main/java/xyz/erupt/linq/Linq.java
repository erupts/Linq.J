package xyz.erupt.linq;

import xyz.erupt.linq.consts.JoinType;
import xyz.erupt.linq.engine.Engine;
import xyz.erupt.linq.engine.EruptEngine;
import xyz.erupt.linq.engine.ParallelEruptEngine;
import xyz.erupt.linq.grammar.*;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.Columns;
import xyz.erupt.linq.util.ReflectField;
import xyz.erupt.linq.util.VirtualColumn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Linq implements Select, Join, Where, GroupBy, OrderBy, Write {

    private Engine engine;

    public Linq() {
    }

    private final Dql dql = new Dql();

    public static Linq from(List<?> data) {
        Linq linq = new Linq();
        linq.dql.setFrom(data);
        return linq;
    }

    @SafeVarargs
    public static <T> Linq from(T... data) {
        return Linq.from(Arrays.stream(data).collect(Collectors.toList()));
    }


    @Override
    public Linq distinct() {
        this.dql.setDistinct(true);
        return this;
    }

    @Override
    public Linq select(Column... columns) {
        for (Column column : columns) {
            this.dql.getColumns().add(column);
        }
        return this;
    }

    @Override
    public <T> Linq select(Class<T> table) {
        List<Column> columns = new ArrayList<>();
        for (Field field : ReflectField.getFields(table)) {
            columns.add(new Column(table, field.getName(), field.getName()));
        }
        this.dql.getColumns().addAll(columns);
        return this;
    }

    @SafeVarargs
    @Override
    public final <T> Linq select(SFunction<T, ?>... columns) {
        for (SFunction<T, ?> column : columns) {
            this.dql.getColumns().add(Columns.of(column));
        }
        return this;
    }

    @Override
    public <T, F> Linq select(SFunction<T, F> column, BiFunction<Row, F, Object> convert) {
        Column col = Columns.of(column);
        col.setRowConvert(row -> convert.apply(row, row.get(column)));
        this.dql.getColumns().add(col);
        return this;
    }

    @SafeVarargs
    @Override
    public final <T> Linq selectExclude(SFunction<T, ?>... columns) {
        this.dql.getColumns().removeIf(it -> {
            for (SFunction<T, ?> column : columns) {
                if (Columns.of(column).equals(it)) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    @Override
    public <T> Linq selectAs(SFunction<T, ?> column, String alias) {
        this.dql.getColumns().add(Columns.of(column, alias));
        return this;
    }

    @Override
    public <T, A> Linq selectAs(SFunction<T, ?> column, SFunction<A, ?> alias) {
        this.dql.getColumns().add(Columns.of(column, LambdaSee.field(alias)));
        return this;
    }

    @Override
    public <T, F> Linq selectAs(SFunction<T, F> column, BiFunction<Row, F, Object> convert, String alias) {
        Column col = Columns.of(column, alias);
        col.setRowConvert(row -> convert.apply(row, row.get(column)));
        this.dql.getColumns().add(col);
        return this;
    }

    @Override
    public <T, A, F> Linq selectAs(SFunction<T, F> column, BiFunction<Row, F, Object> convert, SFunction<A, ?> alias) {
        return selectAs(column, convert, LambdaSee.field(alias));
    }

    @Override
    public Linq selectExpr(Function<Row, Object> convert, String alias) {
        Column column = Columns.of(VirtualColumn::col, alias);
        column.setRowConvert(convert);
        this.dql.getColumns().add(column);
        return this;
    }

    @Override
    public <A> Linq selectExpr(Function<Row, Object> convert, SFunction<A, ?> alias) {
        return selectExpr(convert, LambdaSee.field(alias));
    }

    @Override
    public <T> Linq join(JoinSchema<T> joinSchema) {
        this.dql.getJoinSchemas().add(joinSchema);
        return this;
    }

    @Override
    public <T, S> Linq join(JoinType joinType, List<T> target, SFunction<T, ?> targetOn, SFunction<S, ?> sourceOn) {
        this.dql.getJoinSchemas().add(new JoinSchema<>(joinType, target, targetOn, sourceOn));
        return this;
    }

    @Override
    public Linq orderBy(List<OrderBySchema> orderBySchemas) {
        this.dql.getOrderBys().addAll(orderBySchemas);
        return this;
    }

    @Override
    public Linq where(Predicate<Row> condition) {
        this.dql.getWheres().add(new WhereSchema(condition, null));
        return this;
    }

    // Typed where: besides the row-level condition, record the column + value predicate so the
    // engine can push the filter down to the source objects before row materialization.
    @SuppressWarnings("unchecked")
    @Override
    public <R, S> Linq where(SFunction<R, S> column, Predicate<S> condition) {
        WhereSchema schema = new WhereSchema(row -> condition.test(row.get(column)), Columns.of(column));
        schema.setValueCondition(value -> condition.test((S) value));
        this.dql.getWheres().add(schema);
        return this;
    }

    @Override
    public Linq groupBy(Column... columns) {
        for (Column col : columns) {
            this.dql.getGroupBys().add(col);
        }
        return this;
    }

    @SafeVarargs
    @Override
    public final <T> Linq groupBy(SFunction<T, ?>... columns) {
        Column[] cols = new Column[columns.length];
        for (int i = 0; i < columns.length; i++) {
            cols[i] = Columns.of(columns[i]);
        }
        return groupBy(cols);
    }

    @Override
    public <T, F> Linq groupBy(SFunction<T, F> column, BiFunction<Row, F, Object> convert) {
        Column col = Columns.of(column);
        col.setRowConvert(row -> convert.apply(row, row.get(column)));
        return groupBy(col);
    }

    @Override
    public Linq having(Predicate<Row> condition) {
        this.dql.getHaving().add(condition);
        return this;
    }

    // Materialize the source list in parallel (opt-in). Only kicks in above the engine's
    // threshold; results are order-preserving and identical to the sequential path.
    public Linq parallel() {
        this.engine = new ParallelEruptEngine();
        return this;
    }

    public Linq parallel(int threshold) {
        this.engine = new ParallelEruptEngine(threshold);
        return this;
    }

    public Linq limit(int size) {
        this.dql.setLimit(size);
        return this;
    }

    public Linq offset(int size) {
        this.dql.setOffset(size);
        return this;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public Engine engine() {
        if (null == this.engine) return new EruptEngine();
        return this.engine;
    }

    @Override
    public Dql dql() {
        return this.dql;
    }

}
