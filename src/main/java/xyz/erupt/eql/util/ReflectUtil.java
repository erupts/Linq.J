package xyz.erupt.eql.util;

import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.schema.Column;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    public static <T> T convertMapToObject(Map<Column<?>, Object> map, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<Column<?>, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey().getAlias());
                    field.setAccessible(true);
                    field.set(instance, entry.getValue());
                } catch (NoSuchFieldException ignore) {
                }
            }
            return instance;
        } catch (Exception e) {
            throw new EqlException(e);
        }
    }

}
