package xyz.erupt.eql.schema;

import xyz.erupt.eql.fun.LambdaInfo;
import xyz.erupt.eql.fun.LambdaReflect;
import xyz.erupt.eql.fun.SFunction;

import java.util.Collection;
import java.util.Map;

public class Column {

    private Class<?> table;

    private String field;

    private String alias;

    public Column() {
    }

    public Column(Class<?> table, String field, String alias) {
        this.table = table;
        this.field = field;
        this.alias = alias;
    }

    public Class<?> getTable() {
        return table;
    }

    public void setTable(Class<?> table) {
        this.table = table;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public static Column fromLambda(SFunction<?, ?> fun, String alias) {
        LambdaInfo lambdaInfo = LambdaReflect.getInfo(fun);
        Column column = new Column();
        column.table = lambdaInfo.getClazz();
        column.field = lambdaInfo.getField();
        if (null == alias) {
            column.alias = lambdaInfo.getField();
        } else {
            column.alias = alias;
        }
        return column;
    }

    public static Column fromLambda(SFunction<?, ?> fun) {
        return Column.fromLambda(fun, null);
    }

    public static <R> Column all(Class<R> r) {
        Column column = new Column();
        column.setTable(r);
        return column;
    }

    public static <R> Column of(SFunction<R, ?> fun) {
        return Column.fromLambda(fun);
    }

    public static <R> Column of(Collection<R> r, SFunction<R, ?> fun) {
        return Column.fromLambda(fun);
    }

    public static <R> Column of(Collection<R> r, SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static Column of(SFunction<Map<String, ?>, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column max(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column max(Collection<R> r, SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column count(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column count(Collection<R> r, SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column min(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column min(Collection<R> r, SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column avg(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column avg(Collection<R> r, SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column sum(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column sum(Collection<R> r, SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

}
