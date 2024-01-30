package xyz.erupt.eql.util;

import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.schema.Column;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;

public class ReflectUtil {

    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getActualType(Object o, int index) {
        Type clazz = o.getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) clazz;
        return pt.getActualTypeArguments()[index].getClass();
    }

    public static final Class<?>[] SIMPLE_CLASS = {
            String.class, Character.class, Byte.class, Short.class, Integer.class, Float.class, Double.class, BigDecimal.class,
            String[].class, Character[].class, Byte[].class, Short[].class, Integer[].class, Float[].class, Double[].class, BigDecimal[].class
    };

    public static <T> T convertMapToObject(Map<Column<?>, Object> map, Class<T> clazz) {
        try {
            for (Class<?> sc : SIMPLE_CLASS) {
                if (sc == clazz) {
                    return (T) map.get(map.keySet().iterator().next());
                }
            }
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<Column<?>, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey().getAlias());
                    field.setAccessible(true);
                    if (entry.getValue() instanceof BigDecimal) {
                        field.set(instance, bigDecimalConvert((BigDecimal) entry.getValue(), field.getType()));
                    } else {
                        field.set(instance, entry.getValue());
                    }
                } catch (NoSuchFieldException ignore) {
                }
            }
            return instance;
        } catch (Exception e) {
            throw new EqlException(e);
        }
    }

    private static Object bigDecimalConvert(BigDecimal bigDecimal, Class<?> target) {
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
        return null;
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
