package xyz.erupt.linq;

import xyz.erupt.linq.consts.JoinMethod;
import xyz.erupt.linq.fun.SFunction;
import xyz.erupt.linq.grammar.*;
import xyz.erupt.linq.schema.Dql;
import xyz.erupt.linq.schema.JoinSchema;
import xyz.erupt.linq.schema.Column;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Linq<SOURCE> implements Select<SOURCE>, Join<SOURCE>, Where<SOURCE>, GroupBy<SOURCE>, OrderBy<SOURCE> {

    public Collection<SOURCE> source;

    private final Dql dql = new Dql();

    public <T> List<T> write(T t) {
        Map<String, Void> alias = new HashMap<>();
        for (Column column : this.dql.getColumns()) {
            if (alias.containsKey(column.getAlias())) {
                throw new RuntimeException("Column '" + column.getAlias() + "' is ambiguous");
            }
            alias.put(column.getAlias(), null);
        }
        return null;
    }

    public static <T> Linq<T> from(Collection<T> table) {
        return new Linq<T>() {{
            this.source = table;
        }};
    }

    //TODO 可能没有意义
    public static Linq<String> from(String... t) {
        return new Linq<String>() {{
            this.source = Arrays.asList(t);
        }};
    }


    @Override
    public Linq<SOURCE> distinct() {
        this.dql.setDistinct(true);
        return this;
    }

    public Linq<SOURCE> select(Column... columns) {
        List<Column> cols = new ArrayList<>();
        for (Column column : columns) {
            if (column.getField() == null) {
                // Column.All → select *
                for (Field field : column.getTable().getDeclaredFields()) {
                    cols.add(new Column(column.getTable(), field.getName(), field.getName()));
                }
            } else {
                cols.add(column);
            }
        }
        this.dql.getColumns().addAll(cols);
        return this;
    }

    @Override
    public <R> Linq<SOURCE> orderBy(SFunction<R, ?> column, Direction direction) {
        return this;
    }

    @Override
    public <R> Linq<SOURCE> condition(Column column, Function<Map<Column, ?>, Boolean> fun) {
        this.dql.getConditions().add(fun);
        return this;
    }

    @Override
    public <T1, T2> Linq<SOURCE> join(JoinMethod method, Class<T1> t1, Collection<T2> t2, BiFunction<T1, T2, Boolean> on) {
        this.dql.getJoinSchemas().add(new JoinSchema<>(method, null, t2, on));
        return null;
    }

    @Override
    public <T1> Linq<SOURCE> join(JoinMethod method, Collection<T1> t1, BiFunction<T1, SOURCE, Boolean> on) {
        return null;
    }


    @Override
    public <R> Linq<SOURCE> groupBy(Column... column) {
        this.dql.getGroupBys().addAll(Arrays.asList(column));
        return this;
    }

    @Override
    public Linq<SOURCE> having() {
        return this;
    }

    public Linq<SOURCE> limit(int size) {
        return this;
    }

    public Linq<SOURCE> offset(int size) {
        return this;
    }

}
