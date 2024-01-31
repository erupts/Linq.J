package xyz.erupt.eql;

import xyz.erupt.eql.consts.EqlConst;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.engine.DefaultEngine;
import xyz.erupt.eql.engine.Engine;
import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.grammar.*;
import xyz.erupt.eql.lambda.LambdaReflect;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;
import xyz.erupt.eql.schema.OrderByColumn;
import xyz.erupt.eql.util.Columns;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Linq implements Select, Join, Where, GroupBy, OrderBy, Write {

    private final Engine engine;

    private Linq() {
        this.engine = new DefaultEngine();
    }

    private final Dql dql = new Dql();

    public static <T> Linq from(Collection<T> table) {
        Linq linq = new Linq();
        linq.dql.setSource(table);
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
        Map<String, Void> alias = new HashMap<>();
        for (Column column : this.dql.getColumns()) {
            if (alias.containsKey(column.getAlias())) {
                throw new EqlException("Column '" + column.getAlias() + "' is ambiguous");
            }
            alias.put(column.getAlias(), null);
        }
        return this;
    }

    @Override
    public <T> Linq join(JoinSchema<T> joinSchema) {
        for (JoinSchema<?> schema : this.dql.getJoinSchemas()) {
            if (schema.getClazz() == joinSchema.getClazz()) {
                throw new EqlException(EqlConst.SAME_OBJECT_HINT + " → " + joinSchema.getClazz().getSimpleName());
            }
        }
        this.dql.getJoinSchemas().add(joinSchema);
        return this;
    }

    @Override
    public <T, S> Linq join(JoinMethod joinMethod, Collection<T> target, SFunction<T, Object> onL, SFunction<S, Object> onR) {
        for (JoinSchema<?> join : this.dql.getJoinSchemas()) {
            if (LambdaReflect.getInfo(onL).getClazz() == join.getClazz()) {
                throw new EqlException(EqlConst.SAME_OBJECT_HINT + " → " + join.getClazz().getSimpleName());
            }
        }
        this.dql.getJoinSchemas().add(new JoinSchema<>(joinMethod, target, onL, onR));
        return this;
    }

    @Override
    public <R> Linq orderBy(SFunction<R, ?> column, Direction direction) {
        this.dql.getOrderBys().add(new OrderByColumn(Columns.of(column), direction));
        return this;
    }

    @Override
    public Linq condition(Function<Map<Column, ?>, Boolean> fun) {
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

    @Override
    public Engine $engine() {
        return this.engine;
    }

    @Override
    public Dql $dql() {
        return this.dql;
    }

}
