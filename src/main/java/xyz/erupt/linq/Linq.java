package xyz.erupt.linq;

import xyz.erupt.linq.consts.JoinMethod;
import xyz.erupt.linq.engine.Engine;
import xyz.erupt.linq.engine.EruptEngine;
import xyz.erupt.linq.grammar.*;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.*;
import xyz.erupt.linq.util.Columns;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class Linq implements Select, Join, Where, GroupBy, OrderBy, Write {

    private Engine engine;

    private Linq() {
    }

    private final Dql dql = new Dql();

    public static Linq from(Collection<?> table) {
        Linq linq = new Linq();
        linq.dql.setFrom(table);
        return linq;
    }

    public static <T> Linq from(T row) {
        Linq linq = new Linq();
        linq.dql.setFrom(Collections.singleton(row));
        return linq;
    }


    @Override
    public Linq distinct() {
        this.dql.setDistinct(true);
        return this;
    }

    @Override
    public Linq select(Column... columns) {
        this.dql.getColumns().addAll(Columns.columnsProcess(columns));
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
    public <R> Linq orderBy(SFunction<R, ?> column, Direction direction) {
        this.dql.getOrderBys().add(new OrderByColumn(Columns.of(column), direction));
        return this;
    }

    @Override
    public Linq condition(Function<Row, Boolean> fun) {
        this.dql.getConditions().add(fun);
        return this;
    }


    @Override
    public Linq groupBy(Column... columns) {
        this.dql.getGroupBys().addAll(Columns.columnsProcess(columns));
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

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public Engine $engine() {
        if (null == this.engine) {
            return new EruptEngine();
        }
        return this.engine;
    }

    @Override
    public Dql $dql() {
        return this.dql;
    }

}
