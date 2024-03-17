package xyz.erupt.linq.lambda;

import java.lang.reflect.Field;

public class LambdaInfo {

    private final Class<?> clazz;

    private final Field fieldClazz;

    private final String field;

    public LambdaInfo(Class<?> clazz, Field field) {
        this.clazz = clazz;
        this.fieldClazz = field;
        this.field = field.getName();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getField() {
        return field;
    }

    public Field getFieldClazz() {
        return fieldClazz;
    }
}
