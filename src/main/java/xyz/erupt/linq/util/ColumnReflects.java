package xyz.erupt.linq.util;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.Th;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnReflects {

    public static final Class<?>[] SIMPLE_CLASS = {
            String.class, Character.class, Byte.class, Short.class, Integer.class, Float.class, Double.class, BigDecimal.class
    };

    public static final Class<?>[] SIMPLE_ARR_CLASS = {
            String[].class, Character[].class, Byte[].class, Short[].class, Integer[].class, Float[].class, Double[].class, BigDecimal[].class
    };

    public static List<Row> listToRow(Collection<?> objects) {
        List<Row> list = new ArrayList<>(objects.size());
        for (Object object : objects) {
            Optional.ofNullable(object).ifPresent(it -> list.add(objectToRow(it)));
        }
        return list;
    }

    public static Row objectToRow(Object obj) {
        Row row = new Row();
        for (Class<?> clazz : SIMPLE_CLASS) {
            if (obj.getClass() == clazz) {
                row.put(Columns.of(Th::is), obj);
                return row;
            }
        }
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                row.put(Columns.of(field), field.get(obj));
            }
        } catch (Exception e) {
            throw new LinqException(e);
        }
        return row;
    }

    public static <T> T rowToObject(Row row, Class<T> clazz) {
        for (Class<?> sc : SIMPLE_CLASS) {
            if (sc == clazz) {
                Object val = row.get(row.keySet().iterator().next());
                if (val instanceof BigDecimal) {
                    return (T) bigDecimalConvert((BigDecimal) val, clazz);
                } else {
                    return (T) val;
                }
            }
        }
        for (Class<?> arr : SIMPLE_ARR_CLASS) {
            if (arr == clazz) return (T) row.get(row.keySet().iterator().next());
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Map<String, Field> fieldMap = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, it -> it));
            for (Map.Entry<Column, Object> entry : row.entrySet()) {
                if (fieldMap.containsKey(entry.getKey().getAlias())) {
                    Field field = fieldMap.get(entry.getKey().getAlias());
                    field.setAccessible(true);
                    if (entry.getValue() instanceof BigDecimal) {
                        field.set(instance, bigDecimalConvert((BigDecimal) entry.getValue(), field.getType()));
                    } else {
                        field.set(instance, entry.getValue());
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new LinqException(e);
        }
    }

    public static Object bigDecimalConvert(BigDecimal bigDecimal, Class<?> target) {
        if (Integer.class == target) {
            return bigDecimal.intValue();
        } else if (Short.class == target) {
            return bigDecimal.shortValue();
        } else if (Float.class == target) {
            return bigDecimal.floatValue();
        } else if (Double.class == target) {
            return bigDecimal.doubleValue();
        } else if (Byte.class == target) {
            return bigDecimal.byteValue();
        } else if (BigDecimal.class == target) {
            return bigDecimal;
        }
        throw new LinqException("unknown 'bigDecimal' target type: " + target.getName());
    }

    public static BigDecimal numberToBigDecimal(Number number) {
        if (number instanceof Integer) {
            return BigDecimal.valueOf((Integer) number);
        } else if (number instanceof Short) {
            return BigDecimal.valueOf((Short) number);
        } else if (number instanceof Float) {
            return BigDecimal.valueOf((Float) number);
        } else if (number instanceof Double) {
            return BigDecimal.valueOf((Double) number);
        } else if (number instanceof Byte) {
            return BigDecimal.valueOf((Byte) number);
        } else if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else {
            return BigDecimal.valueOf(number.doubleValue());
        }
    }

}
