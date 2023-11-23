package xyz.erupt.eql.schema;

import xyz.erupt.eql.fun.LambdaInfo;
import xyz.erupt.eql.fun.LambdaReflect;
import xyz.erupt.eql.fun.SFunction;

import java.util.Collection;
import java.util.Map;

public class Column<T> {

    private Class<T> table;

    private String field;

    private String alias;

    public Column() {
    }

    public Column(Class<T> table, String field, String alias) {
        this.table = table;
        this.field = field;
        this.alias = alias;
    }

    public Class<?> getTable() {
        return table;
    }

    public void setTable(Class<T> table) {
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

    public static <T> Column<T> fromLambda(SFunction<T, ?> fun, String alias) {
        LambdaInfo<T> lambdaInfo = LambdaReflect.getInfo(fun);
        Column<T> column = new Column<>();
        column.table = lambdaInfo.getClazz();
        column.field = lambdaInfo.getField();
        if (null == alias) {
            column.alias = lambdaInfo.getField();
        } else {
            column.alias = alias;
        }
        return column;
    }

    public static <T> Column<T> fromLambda(SFunction<T, ?> fun) {
        return Column.fromLambda(fun, null);
    }

    public static <R> Column<R> all(Class<R> r) {
        Column<R> column = new Column<R>();
        column.setTable(r);
        return column;
    }

    public static <R> Column<R> of(SFunction<R, ?> fun) {
        return Column.fromLambda(fun);
    }

    public static <R> Column<R> of(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static Column<?> ofs(SFunction<Map<String, ?>, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    public static <R> Column<R> max(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    //
    public static <R> Column<R> count(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

    //
//
    public static <R> Column<R> min(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }


    public static <R> Column<R> avg(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }


    public static <R> Column<R> sum(SFunction<R, ?> fun, String alias) {
        return Column.fromLambda(fun, alias);
    }

}
