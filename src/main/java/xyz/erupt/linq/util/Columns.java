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

    private static <T> Column fromLambda(SFunction<T, ?> fun) {
        LambdaInfo lambdaInfo = LambdaReflect.info(fun);
        return fromLambda(fun, lambdaInfo.getField());
    }

    private static <T> Column fromLambda(SFunction<T, ?> fun, String alias) {
        LambdaInfo lambdaInfo = LambdaReflect.info(fun);
        Column column = new Column();
        column.setTable(lambdaInfo.getClazz());
        column.setField(lambdaInfo.getField());
        column.setAlias(alias);
        return column;
    }

    // Column.All → select *
    public static <R> Column all(Class<R> r) {
        Column column = new Column();
        column.setUnfold(() -> {
            List<Column> columns = new ArrayList<>();
            for (Field field : ReflectField.getFields(r)) {
                columns.add(new Column(r, field.getName(), field.getName()));
            }
            return columns;
        });
        return column;
    }

    public static <R, S> Column ofx(SFunction<R, S> fun, Function<S, Object> convert, String alias) {
        Column column = Columns.fromLambda(fun);
        column.setAlias(alias);
        column.setRowConvert(row -> convert.apply(row.get(fun)));
        return column;
    }

    public static <R, S, A> Column ofx(SFunction<R, S> fun, Function<S, Object> convert, SFunction<A, ?> alias) {
        return ofx(fun, convert, LambdaReflect.field(alias));
    }

    public static <R, S> Column ofx(SFunction<R, S> fun, Function<S, Object> convert) {
        return ofx(fun, convert, LambdaReflect.field(fun));
    }


    public static <R> Column of(SFunction<R, ?> fun) {
        return Columns.fromLambda(fun);
    }

    public static <R> Column of(SFunction<R, ?> fun, String alias) {
        return Columns.fromLambda(fun, alias);
    }

    public static <R, A> Column of(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return of(fun, LambdaReflect.field(alias));
    }

    public static Column ofs(Function<Row, ?> fun, String alias) {
        Column column = new Column();
        column.setTable(VirtualColumn.class);
        column.setField(VirtualColumn.lambdaInfo().getField());
        column.setAlias(alias);
        column.setRowConvert(fun);
        return column;
    }

    public static <A> Column ofs(Function<Row, ?> fun, SFunction<A, ?> alias) {
        return ofs(fun, LambdaReflect.field(alias));
    }

    public static Column count(String alias) {
        Column column = new Column(VirtualColumn.class, VirtualColumn.lambdaInfo().getField(), alias);
        column.setGroupByFun(it -> BigDecimal.valueOf(it.size()));
        return column;
    }

    public static <A> Column count(SFunction<A, ?> alias) {
        return count(LambdaReflect.field(alias));
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
        return count(fun, LambdaReflect.field(alias));
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
        return countDistinct(fun, LambdaReflect.field(alias));
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
        return max(fun, LambdaReflect.field(alias));
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
        return min(fun, LambdaReflect.field(alias));
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
        return avg(fun, LambdaReflect.field(alias));
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
        return sum(fun, LambdaReflect.field(alias));
    }

    public static <R> Column groupArray(SFunction<R, ?> fun, String alias) {
        return groupByProcess(fun, alias, (column, list) -> {
            List<Object> result = new ArrayList<>();
            for (Row row : list) {
                Optional.ofNullable(row.get(column)).ifPresent(result::add);
            }
            return result;
        });
    }

    public static <R, A> Column groupArray(SFunction<R, ?> fun, SFunction<A, ?> alias) {
        return groupArray(fun, LambdaReflect.field(alias));
    }

    // custom group by logic
    public static <R> Column groupByProcess(SFunction<R, ?> fun, String alias, BiFunction<Column, List<Row>, Object> groupByProcess) {
        Column column = Columns.of(fun, alias);
        column.setGroupByFun(it -> groupByProcess.apply(column.getRawColumn(), it));
        return column;
    }

    // column common process
    public static List<Column> columnsUnfold(Column... columns) {
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
