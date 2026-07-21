package xyz.erupt.linq.util;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.It;
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
        return listToTable(objects, false);
    }

    /**
     * @param parallel materialize rows across the common ForkJoinPool. Only worthwhile for
     *                 large datasets; encounter order is preserved so downstream stages behave
     *                 identically to the sequential path.
     */
    public static List<Row> listToTable(List<?> objects, boolean parallel) {
        int size = objects.size();
        List<Row> list = new ArrayList<>(Math.min(size, 10000));
        if (size == 0) {
            return list;
        }

        // Find first non-null object to determine class - optimize by checking common case first
        Object firstObj;
        firstObj = objects.get(0);
        if (firstObj == null && size > 1) {
            // Only search if first is null
            for (int i = 1; i < size; i++) {
                firstObj = objects.get(i);
                if (firstObj != null) {
                    break;
                }
            }
        }
        if (firstObj == null) {
            return list;
        }

        Class<?> clazz = firstObj.getClass();
        List<Field> fields = ReflectField.getFields(clazz);
        boolean simpleClass = isSimpleClass(clazz);

        // Pre-create Column objects and set fields accessible
        Map<String, Column> columnCache = null;
        Column simpleColumn = null;
        if (simpleClass) {
            simpleColumn = Columns.of(It::self);
        } else {
            columnCache = new java.util.HashMap<>(fields.size());
            for (Field field : fields) {
                if (!field.isAccessible()) field.setAccessible(true);
                columnCache.put(field.getName(), new Column(clazz, field.getName(), field.getName()));
            }
        }

        // Process all objects
        if (simpleClass) {
            // Simple class path - optimized
            for (int i = 0; i < size; i++) {
                Object obj = objects.get(i);
                if (obj != null) {
                    Row row = new Row(1);
                    row.put(simpleColumn, obj);
                    list.add(row);
                }
            }
        } else {
            int fieldCount = fields.size();
            Column[] columnArray = new Column[fieldCount];
            Field[] fieldArray = fields.toArray(new Field[fieldCount]);
            // LambdaMetafactory-generated getters (JIT-inlinable). null entry -> reflect that field.
            @SuppressWarnings("unchecked")
            java.util.function.Function<Object, Object>[] getters = new java.util.function.Function[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                columnArray[i] = columnCache.get(fieldArray[i].getName());
                getters[i] = Accessors.getter(clazz, fieldArray[i].getName(), fieldArray[i].getType());
            }

            if (parallel) {
                // Encounter order preserved -> result is identical to the sequential path.
                return java.util.stream.IntStream.range(0, size).parallel()
                        .mapToObj(i -> buildRow(objects.get(i), fieldCount, columnArray, fieldArray, getters))
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
            }
            for (int i = 0; i < size; i++) {
                Row row = buildRow(objects.get(i), fieldCount, columnArray, fieldArray, getters);
                if (row != null) list.add(row);
            }
        }
        return list;
    }

    private static Row buildRow(Object obj, int fieldCount, Column[] columns, Field[] fields,
                                java.util.function.Function<Object, Object>[] getters) {
        if (obj == null) return null;
        // Row with exact capacity to avoid resizing
        Row row = new Row(fieldCount);
        for (int j = 0; j < fieldCount; j++) {
            Object value;
            if (getters[j] != null) {
                value = getters[j].apply(obj);
            } else {
                try {
                    value = fields[j].get(obj);
                } catch (IllegalAccessException e) {
                    throw new LinqException(e);
                }
            }
            if (null != value) {
                row.putDirect(columns[j], value);
            }
        }
        return row;
    }

    public static boolean isSimpleClass(Class<?> clazz) {
        for (Class<?> aClass : SIMPLE_CLASS) {
            if (aClass.isAssignableFrom(clazz)) return true;
        }
        return false;
    }

    /**
     * Per-target-class write plan: no-arg constructor, writable fields and (when a public
     * setter exists) LambdaMetafactory-generated setters — resolved once, reused per row.
     */
    private static final class WriteMeta {
        final java.lang.reflect.Constructor<?> ctor;
        final Map<String, Field> fieldMap;
        final Map<String, java.util.function.BiConsumer<Object, Object>> setters;

        WriteMeta(Class<?> clazz) {
            try {
                this.ctor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new LinqException(e);
            }
            if (!this.ctor.isAccessible()) this.ctor.setAccessible(true);
            List<Field> fields = ReflectField.getFields(clazz);
            this.fieldMap = new java.util.HashMap<>(fields.size());
            this.setters = new java.util.HashMap<>(fields.size());
            for (Field field : fields) {
                if (!field.isAccessible()) field.setAccessible(true);
                this.fieldMap.put(field.getName(), field);
                java.util.function.BiConsumer<Object, Object> setter = Accessors.setter(clazz, field.getName(), field.getType());
                if (null != setter) this.setters.put(field.getName(), setter);
            }
        }
    }

    private static final Map<Class<?>, WriteMeta> WRITE_META_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static <T> T rowToObject(Row row, Class<T> clazz) {
        int rowSize = row.size();
        if (rowSize == 1) {
            // Single-column result: value maps straight through, no reflection needed
            Object firstVal = row.valueAt(0);
            if (firstVal != null && clazz == firstVal.getClass()) return (T) firstVal;
            // Use array access for better performance
            for (Class<?> simpleClass : SIMPLE_CLASS) {
                if (simpleClass.isAssignableFrom(clazz)) {
                    return (T) (firstVal instanceof BigDecimal ? bigDecimalConvert((BigDecimal) firstVal, clazz) : firstVal);
                }
            }
        }
        try {
            WriteMeta meta = WRITE_META_CACHE.computeIfAbsent(clazz, WriteMeta::new);
            @SuppressWarnings("unchecked")
            T instance = (T) meta.ctor.newInstance();
            // Positional iteration avoids allocating an Entry object per column
            for (int i = 0; i < rowSize; i++) {
                String alias = row.columnAt(i).getAlias();
                Field field = meta.fieldMap.get(alias);
                if (field != null) {
                    Object value = row.valueAt(i);
                    if (value instanceof BigDecimal) {
                        value = bigDecimalConvert((BigDecimal) value, field.getType());
                    }
                    java.util.function.BiConsumer<Object, Object> setter = meta.setters.get(alias);
                    if (null != setter) {
                        setter.accept(instance, value);
                    } else {
                        try {
                            field.set(instance, value);
                        } catch (IllegalAccessException e) {
                            throw new LinqException(e);
                        }
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
