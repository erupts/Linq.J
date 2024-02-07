package xyz.erupt.linq;

import xyz.erupt.linq.consts.JoinMethod;
import xyz.erupt.linq.engine.Engine;
import xyz.erupt.linq.engine.EruptEngine;
import xyz.erupt.linq.grammar.*;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.lambda.Th;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.Columns;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Linq implements Select, Join, Where, GroupBy, OrderBy, Write {

    private Engine engine;

    public Linq() {
    }

    private final Dql dql = new Dql();


    public static Linq from(Collection<?> table) {
        Linq linq = new Linq();
        linq.dql.setFrom(table);
        return linq;
    }

    @SafeVarargs
    public static <T> Linq from(T... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList()));
    }

    public static Linq from(Boolean... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(Byte... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(Character... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(String... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(Short... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(Integer... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(Long... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    public static Linq from(Float... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }


    public static Linq from(Double... table) {
        return Linq.from(Arrays.stream(table).collect(Collectors.toList())).select(Columns.of(Th::is));
    }

    @Override
    public Linq distinct() {
        this.dql.setDistinct(true);
        return this;
    }

    @Override
    public Linq select(Column column, Column... columns) {
        this.dql.getColumns().addAll(Columns.columnsUnfold(column));
        this.dql.getColumns().addAll(Columns.columnsUnfold(columns));
        return this;
    }

    @Override
    @SafeVarargs
    public final <T> Linq select(SFunction<T, ?> column, SFunction<T, ?>... columns) {
        this.dql.getColumns().add(Columns.of(column));
        for (SFunction<T, ?> col : columns) {
            this.dql.getColumns().add(Columns.of(col));
        }
        return this;
    }

    @Override
    public <T> Linq join(JoinSchema<T> joinSchema) {
        this.dql.getJoinSchemas().add(joinSchema);
        return this;
    }

    @Override
    public <T, S> Linq join(JoinMethod joinMethod, Collection<T> target, SFunction<T, Object> onL, SFunction<S, Object> onR) {
        this.dql.getJoinSchemas().add(new JoinSchema<>(joinMethod, target, onL, onR));
        return this;
    }

    @Override
    public Linq orderBy(List<OrderBySchema> orderBySchemas) {
        this.dql.getOrderBys().addAll(orderBySchemas);
        return this;
    }

    @Override
    public Linq condition(Function<Row, Boolean> fun) {
        this.dql.getConditions().add(fun);
        return this;
    }

    @SafeVarargs
    @Override
    public final <T> Linq groupBy(SFunction<T, ?> column, SFunction<T, ?>... columns) {
        this.dql.getGroupBys().add(Columns.of(column));
        for (SFunction<T, ?> col : columns) {
            this.dql.getGroupBys().add(Columns.of(col));
        }
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
    public Engine wEngine() {
        if (null == this.engine) return new EruptEngine();
        return this.engine;
    }

    @Override
    public Dql wDQL() {
        return this.dql;
    }

}
