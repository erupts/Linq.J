package xyz.erupt.linq.util;

import xyz.erupt.linq.consts.CompareSymbol;
import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaReflect;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Columns {

    public static <T> Column fromLambda(SFunction<T, ?> fun, String alias) {
        LambdaInfo lambdaInfo = LambdaReflect.getInfo(fun);
        Column column = new Column();
        column.setTable(lambdaInfo.getClazz());
        column.setField(lambdaInfo.getField());
        if (null == alias) {
            column.setAlias(lambdaInfo.getField());
        } else {
            column.setAlias(alias);
        }
        return column;
    }

    public static <T> Column fromLambda(SFunction<T, ?> fun) {
        return Columns.fromLambda(fun, null);
    }

    public static Column fromField(Field field) {
        Column column = new Column();
        column.setTable(field.getDeclaringClass());
        column.setField(field.getName());
        column.setAlias(field.getName());
        return column;
    }

    // Column.All → select *
    public static <R> Column all(Class<R> r) {
        Column column = new Column();
        column.setUnfold(() -> {
            List<Column> columns = new ArrayList<>();
            for (Field field : r.getDeclaredFields()) {
                columns.add(new Column(r, field.getName(), field.getName()));
            }
            return columns;
        });
        return column;
    }

    public static <R> Column of(SFunction<R, ?> fun) {
        return Columns.fromLambda(fun);
    }

    public static <R> Column of(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    public static <R, A> Column of(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return of(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static Column ofs(Function<Row, ?> fun, String alias) {
        Column column = new Column();
        column.setTable(VirtualColumn.class);
        column.setField(VirtualColumn.lambdaInfo().getField());
        column.setAlias(alias);
        column.setRowValueProcess(fun);
        return column;
    }

    public static <A> Column ofs(Function<Row, ?> fun, SFunction<A, ?> alias) {
        return ofs(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static Column count(String alias) {
        Column column = new Column(VirtualColumn.class, VirtualColumn.lambdaInfo().getField(), alias);
        column.setGroupByFun(it -> BigDecimal.valueOf(it.size()));
        return column;
    }

    public static <A> Column count(SFunction<A, ?> alias) {
        return count(LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column count(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            int i = 0;
            for (Row row : list) {
                if (null != row.get(column)) i++;
            }
            return BigDecimal.valueOf(i);
        });
    }

    public static <R, A> Column count(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return count(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column countDistinct(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            Map<Object, Void> distinctMap = new HashMap<>();
            for (Row row : list) {
                Optional.ofNullable(row.get(column)).ifPresent(it -> distinctMap.put(it, null));
            }
            return BigDecimal.valueOf(distinctMap.size());
        });
    }

    public static <R, A> Column countDistinct(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return countDistinct(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column max(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            Object result = null;
            for (Row row : list) {
                Object val = row.get(column);
                if (null == result) result = val;
                if (CompareUtil.compare(val, result, CompareSymbol.GT)) result = val;
            }
            if (result instanceof Number) {
                return ColumnReflects.numberToBigDecimal((Number) result);
            } else {
                return result;
            }
        });
    }


    public static <R, A> Column max(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return max(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column min(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            Object result = null;
            for (Row row : list) {
                Object val = row.get(column);
                if (null == result) result = val;
                if (CompareUtil.compare(val, result, CompareSymbol.LT)) result = val;
            }
            if (result instanceof Number) {
                return ColumnReflects.numberToBigDecimal((Number) result);
            } else {
                return result;
            }
        });
    }

    public static <R, A> Column min(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return min(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column avg(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            BigDecimal bigDecimal = new BigDecimal(0);
            int count = 0;
            for (Row row : list) {
                Object val = row.get(column);
                if (val instanceof Number) {
                    bigDecimal = bigDecimal.add(new BigDecimal(String.valueOf(val)));
                    count++;
                }
            }
            return count > 0 ? BigDecimal.valueOf(bigDecimal.doubleValue() / count) : BigDecimal.valueOf(0);
        });
    }

    public static <R, A> Column avg(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return avg(fun, LambdaReflect.getInfo(alias).getField());
    }

    public static <R> Column sum(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            BigDecimal bigDecimal = new BigDecimal(0);
            for (Row row : list) {
                Object val = row.get(column);
                if (val instanceof Number) {
                    bigDecimal = bigDecimal.add(new BigDecimal(String.valueOf(val)));
                }
            }
            return bigDecimal;
        });
    }

    public static <R, A> Column sum(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return sum(fun, LambdaReflect.getInfo(alias).getField());
    }

    // custom group by logic
    public static <R> Column groupByProcess(SFunction<R, ?> fun, String alias, BiFunction<Column, List<Row>, Object> groupByFun) {
        Column column = Columns.fromLambda(fun, alias);
        column.setGroupByFun(it -> groupByFun.apply(column.getRawColumn(), it));
        return column;
    }

    //column common process
    public static List<Column> columnsProcess(Column... columns) {
        List<Column> cols = new ArrayList<>();
        for (Column column : columns) {
            if (null == column.getUnfold()) {
                cols.add(column);
            } else {
                cols.addAll(column.getUnfold().get());
            }
        }
        return cols;
    }

}
