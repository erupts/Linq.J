package xyz.erupt.linq.util;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.Th;
import xyz.erupt.linq.schema.Column;
import xyz.erupt.linq.schema.Row;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RowUtil {

    public static final Class<?>[] SIMPLE_CLASS = {
            CharSequence.class, Character.class, Number.class, Date.class, Temporal.class, Boolean.class,
            CharSequence[].class, Character[].class, Number[].class, Date[].class, Temporal[].class, Boolean[].class
    };

    public static List<Row> listToTable(List<?> objects) {
        List<Row> list = new ArrayList<>(objects.size());
        Class<?> clazz = null;
        List<Field> fields = null;
        boolean simpleClass = false;
        boolean first = true;
        for (Object obj : objects) {
            if (null != obj) {
                if (first) {
                    clazz = obj.getClass();
                    fields = ReflectField.getFields(clazz);
                    for (Class<?> sc : SIMPLE_CLASS) {
                        if (sc.isAssignableFrom(clazz)) simpleClass = true;
                    }
                    first = false;
                }
                if (simpleClass) {
                    list.add(new Row(1) {{
                        this.put(Columns.of(Th::is), obj);
                    }});
                } else {
                    Row row = new Row(fields.size());
                    try {
                        for (Field field : fields) {
                            if (!field.isAccessible()) field.setAccessible(true);
                            row.put(new Column(clazz, field.getName(), field.getName()), field.get(obj));
                        }
                    } catch (Exception e) {
                        throw new LinqException(e);
                    }
                    list.add(row);
                }
            }
        }
        return list;
    }

    public static <T> T rowToObject(Row row, Class<T> clazz) {
        if (row.size() == 1) {
            Object val = row.get(row.keySet().iterator().next());
            if (null != val && clazz == val.getClass()) return (T) val;
            for (Class<?> sc : SIMPLE_CLASS) {
                if (sc.isAssignableFrom(clazz)) {
                    return (T) (val instanceof BigDecimal ? bigDecimalConvert((BigDecimal) val, clazz) : val);
                }
            }
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Map<String, Field> fieldMap = ReflectField.getFields(clazz).stream().collect(Collectors.toMap(Field::getName, it -> it));
            for (Map.Entry<Column, Object> entry : row.entrySet()) {
                if (fieldMap.containsKey(entry.getKey().getAlias())) {
                    Field field = fieldMap.get(entry.getKey().getAlias());
                    if (!field.isAccessible()) field.setAccessible(true);
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
        } else if (Long.class == target) {
            return bigDecimal.longValue();
        } else if (BigDecimal.class == target) {
            return bigDecimal;
        } else if (target.isAssignableFrom(String.class)) {
            return bigDecimal.toString();
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
