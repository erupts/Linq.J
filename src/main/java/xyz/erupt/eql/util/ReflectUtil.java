package xyz.erupt.eql.util;

import java.lang.reflect.Field;

public class ReflectUtil {

    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
