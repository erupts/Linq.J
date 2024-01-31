package xyz.erupt.eql.util;

import xyz.erupt.eql.consts.CompareSymbol;
import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.lambda.LambdaReflect;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

public class Columns {

    public static <T> Column<T> fromLambda(SFunction<T, ?> fun, String alias) {
        LambdaInfo lambdaInfo = LambdaReflect.getInfo(fun);
        Column<T> column = new Column<>();
        column.setTable((Class<T>) lambdaInfo.getClazz());
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
        Column<R> column = new Column<>();
        column.setTable(r);
        return column;
    }

    public static <R> Column<R> of(SFunction<R, ?> fun) {
        return Columns.fromLambda(fun);
    }

    public static <R> Column<R> of(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    public static <R, A> Column<R> of(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return of(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static Column<?> ofs(SFunction<Map<String, ?>, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    public static <A> Column<?> ofs(SFunction<Map<String, ?>, ?> fun, SFunction<A, ?> alias) {
        return Columns.fromLambda(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static Column<VirtualModel> count(String alias) {
        Column<VirtualModel> column = new Column<>(VirtualModel.class, String.class, "number", alias);
        column.setGroupByFun(it -> BigDecimal.valueOf(it.size()));
        return column;
    }

    public static <A> Column<?> count(SFunction<A, ?> alias) {
        return count(LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column<R> count(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            int i = 0;
            for (Map<Column<?>, ?> map : list) {
                if (null != map.get(column)) i++;
            }
            return BigDecimal.valueOf(i);
        });
    }

    public static <R, A> Column<R> count(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return count(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column<R> countDistinct(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            Map<Object, Void> distinctMap = new HashMap<>();
            for (Map<Column<?>, ?> map : list) {
                Optional.ofNullable(map.get(column)).ifPresent(it -> distinctMap.put(it, null));
            }
            return BigDecimal.valueOf(distinctMap.size());
        });
    }

    public static <R, A> Column<R> countDistinct(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return countDistinct(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column<R> max(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            Object result = null;
            for (Map<Column<?>, ?> map : list) {
                Object val = map.get(column);
                if (null == result) {
                    result = val;
                }
                if (CompareUtil.compare(val, result, CompareSymbol.GT)) {
                    result = val;
                }
            }
            if (result instanceof Number) {
                return ReflectUtil.numberToBigDecimal((Number) result);
            } else {
                return result;
            }
        });
    }


    public static <R, A> Column<R> max(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return max(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column<R> min(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            Object result = null;
            for (Map<Column<?>, ?> map : list) {
                Object val = map.get(column);
                if (null == result) {
                    result = val;
                }
                if (CompareUtil.compare(val, result, CompareSymbol.LT)) {
                    result = val;
                }
            }
            if (result instanceof Number) {
                return ReflectUtil.numberToBigDecimal((Number) result);
            } else {
                return result;
            }
        });
    }

    public static <R, A> Column<R> min(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return min(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column<R> avg(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            BigDecimal bigDecimal = new BigDecimal(0);
            int count = 0;
            for (Map<Column<?>, ?> map : list) {
                Object val = map.get(column);
                if (val instanceof Number) {
                    bigDecimal = bigDecimal.add(new BigDecimal(String.valueOf(val)));
                    count++;
                }
            }
            return count > 0 ? BigDecimal.valueOf(bigDecimal.doubleValue() / count) : BigDecimal.valueOf(0);
        });
    }

    public static <R, A> Column<R> avg(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return avg(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column<R> sum(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            BigDecimal bigDecimal = new BigDecimal(0);
            for (Map<Column<?>, ?> map : list) {
                Object val = map.get(column);
                if (val instanceof Number) {
                    bigDecimal = bigDecimal.add(new BigDecimal(String.valueOf(val)));
                }
            }
            return bigDecimal;
        });
    }

    public static <R, A> Column<R> sum(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return sum(fun, LambdaReflect.getInfo(alias).getField());
    }

    //自定义分组处理函数
    public static <R> Column<R> groupByProcess(SFunction<R, ?> fun, String alias, BiFunction<Column<?>, List<Map<Column<?>, Object>>, Object> groupByFun) {
        Column<R> column = Columns.fromLambda(fun, alias);
        column.setGroupByFun(it -> groupByFun.apply(column.getRawColumn(), it));
        return column;
    }

    //column common process
    public static List<Column<?>> columnsProcess(Column<?>... columns) {
        List<Column<?>> cols = new ArrayList<>();
        for (Column<?> column : columns) {
            if (column.getField() == null) {
                // Column.All → select *
                if (null != column.getTable()) {
                    for (Field field : column.getTable().getDeclaredFields()) {
                        cols.add(new Column<>(column.getTable(), field.getType(), field.getName(), field.getName()));
                    }
                }
            } else {
                cols.add(column);
            }
        }
        return cols;
    }

}
