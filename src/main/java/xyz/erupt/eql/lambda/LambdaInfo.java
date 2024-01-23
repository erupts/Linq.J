package xyz.erupt.eql.lambda;

import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.util.Columns;

import java.lang.reflect.Field;
import java.util.*;

public class LambdaInfo<T> {

    private final Class<T> clazz;

    private final String field;

    public LambdaInfo(Class<T> clazz, String field) {
        this.clazz = clazz;
        this.field = field;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getField() {
        return field;
    }

    public static Map<Column<?>, Object> objectToColumnMap(Object obj) {
        Map<Column<?>, Object> columnObjectMap = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                columnObjectMap.put(Columns.fromField(field), field.get(obj));
            }
        } catch (Exception e) {
            throw new EqlException(e);
        }
        return columnObjectMap;
    }

    public static List<Map<Column<?>, Object>> objectToLambdaInfos(Collection<?> objects) {
        List<Map<Column<?>, Object>> list = new ArrayList<>();
        for (Object object : objects) {
            Optional.ofNullable(object).ifPresent(it -> list.add(objectToColumnMap(it)));
        }
        return list;
    }

}
