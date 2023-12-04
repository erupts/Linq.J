package xyz.erupt.eql.util;

import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.lambda.LambdaReflect;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;

import java.lang.reflect.Field;
import java.util.Map;

public class Columns {

    public static <T> Column<T> fromLambda(SFunction<T, ?> fun, String alias) {
        LambdaInfo<T> lambdaInfo = LambdaReflect.getInfo(fun);
        Column<T> column = new Column<>();
        column.setTable(lambdaInfo.getClazz());
        column.setField(lambdaInfo.getField());
        if (null == alias) {
            column.setAlias(lambdaInfo.getField());
        } else {
            column.setAlias(alias);
        }
        return column;
    }

    public static <T> Column<T> fromLambda(SFunction<T, ?> fun) {
        return Columns.fromLambda(fun, null);
    }

    public static <T> Column<T> fromField(Field field) {
        Column<T> column = new Column<>();
        column.setTable((Class<T>) field.getDeclaringClass());
        column.setField(field.getName());
        column.setAlias(field.getName());
        return column;
    }

    public static <R> Column<R> all(Class<R> r) {
        Column<R> column = new Column<R>();
        column.setTable(r);
        return column;
    }

    public static <R> Column<R> of(SFunction<R, ?> fun) {
        return Columns.fromLambda(fun);
    }

    public static <R> Column<R> of(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    public static Column<?> ofs(SFunction<Map<String, ?>, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    public static <R> Column<R> max(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    //
    public static <R> Column<R> count(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    //
//
    public static <R> Column<R> min(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }


    public static <R> Column<R> avg(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }


    public static <R> Column<R> sum(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

}
