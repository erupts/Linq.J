package xyz.erupt.eql;

import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.fun.SFunction;
import xyz.erupt.eql.grammar.*;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Linq implements Select, Join, Where, GroupBy, OrderBy {

    private Linq() {
    }

    private final Dql dql = new Dql();

    public <T> List<T> write(Class<T> clazz) {
        this.dql.setTarget(clazz);
        return null;
    }

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
    public Linq select(Column<?>... columns) {
        List<Column<?>> cols = new ArrayList<>();
        for (Column<?> column : columns) {
            if (column.getField() == null) {
                // Column.All → select *
                for (Field field : column.getTable().getDeclaredFields()) {
                    cols.add(new Column<>(column.getTable(), field.getName(), field.getName()));
                }
            } else {
                cols.add(column);
            }
        }
        this.dql.getColumns().addAll(cols);
        Map<String, Void> alias = new HashMap<>();
        for (Column<?> column : this.dql.getColumns()) {
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
                throw new EqlException("The same object join is not supported → " + joinSchema.getClazz().getSimpleName());
            }
        }
        this.dql.getJoinSchemas().add(joinSchema);
        return this;
    }

    @Override
    public <R> Linq orderBy(SFunction<R, ?> column, Direction direction) {
        return this;
    }

    @Override
    public <R> Linq condition(Column<R> column, Function<Map<Column<R>, ?>, Boolean> fun) {
        this.dql.getConditions().add(fun);
        return this;
    }


    @Override
    public <R> Linq groupBy(Column<?>... column) {
        this.dql.getGroupBys().addAll(Arrays.asList(column));
        return this;
    }

    @Override
    public Linq having() {
        return this;
    }

    public Linq limit(long size) {
        this.dql.setLimit(size);
        return this;
    }

    public Linq offset(long size) {
        this.dql.setOffset(size);
        return this;
    }

}
