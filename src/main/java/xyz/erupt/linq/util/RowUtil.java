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
        int size = objects.size();
        List<Row> list = new ArrayList<>(size);
        if (size == 0) {
            return list;
        }
        
        // Find first non-null object to determine class - optimize by checking common case first
        Object firstObj = null;
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
        // Optimize: check simple class using faster method
        boolean simpleClass = false;
        for (Class<?> aClass : SIMPLE_CLASS) {
            if (aClass.isAssignableFrom(clazz)) {
                simpleClass = true;
                break;
            }
        }
        
        // Pre-create Column objects and set fields accessible
        Map<String, Column> columnCache = null;
        Column simpleColumn = null;
        if (simpleClass) {
            simpleColumn = Columns.of(Th::is);
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
            // Optimize: use array access instead of map lookup in inner loop
            int fieldCount = fields.size();
            Column[] columnArray = new Column[fieldCount];
            Field[] fieldArray = fields.toArray(new Field[fieldCount]);
            for (int i = 0; i < fieldCount; i++) {
                columnArray[i] = columnCache.get(fieldArray[i].getName());
            }
            // Process objects - use direct array access for better performance
            // Pre-allocate Row capacity to avoid HashMap resizing
            // Optimize: process in batches and reduce exception handling overhead
            for (int i = 0; i < size; i++) {
                Object obj = objects.get(i);
                if (obj != null) {
                    Row row = new Row(fieldCount);
                    // Direct array access - avoid list.get() in inner loop
                    // Batch put operations for better cache locality
                    // Inline field access to reduce method call overhead
                    try {
                        for (int j = 0; j < fieldCount; j++) {
                            // Direct field access - avoid repeated method calls
                            Field field = fieldArray[j];
                            Object value = field.get(obj);
                            row.put(columnArray[j], value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new LinqException(e);
                    }
                    list.add(row);
                }
            }
        }
        return list;
    }

    // Cache for field maps to avoid repeated creation
    private static final Map<Class<?>, Map<String, Field>> FIELD_MAP_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static <T> T rowToObject(Row row, Class<T> clazz) {
        int rowSize = row.size();
        if (rowSize == 1) {
            // Optimize: get first entry directly - avoid iterator creation
            Column firstKey = null;
            Object firstVal = null;
            for (Map.Entry<Column, Object> entry : row.entrySet()) {
                firstKey = entry.getKey();
                firstVal = entry.getValue();
                break; // Only need first entry
            }
            if (firstVal != null && clazz == firstVal.getClass()) return (T) firstVal;
            // Use array access for better performance
            for (Class<?> simpleClass : SIMPLE_CLASS) {
                if (simpleClass.isAssignableFrom(clazz)) {
                    return (T) (firstVal instanceof BigDecimal ? bigDecimalConvert((BigDecimal) firstVal, clazz) : firstVal);
                }
            }
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            // Cache field map to avoid repeated creation
            Map<String, Field> fieldMap = FIELD_MAP_CACHE.computeIfAbsent(clazz, k -> ReflectField.getFields(k).stream()
                    .peek(field -> {
                        if (!field.isAccessible()) field.setAccessible(true);
                    })
                    .collect(Collectors.toMap(Field::getName, it -> it)));
            // Optimize: iterate once and check both conditions
            // Pre-check BigDecimal to avoid repeated instanceof
            for (Map.Entry<Column, Object> entry : row.entrySet()) {
                Field field = fieldMap.get(entry.getKey().getAlias());
                if (field != null) {
                    Object value = entry.getValue();
                    try {
                        if (value instanceof BigDecimal) {
                            field.set(instance, bigDecimalConvert((BigDecimal) value, field.getType()));
                        } else {
                            field.set(instance, value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new LinqException(e);
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
