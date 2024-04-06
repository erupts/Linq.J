package xyz.erupt.linq.util;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YuePeng
 * date 2024/4/3 20:55
 */
public class ReflectField {

    private static final Map<Class<?>, List<Field>> FIELDS_CACHE = new ConcurrentHashMap<>();

    public static List<Field> getFields(Class<?> clazz) throws SecurityException {
        return FIELDS_CACHE.computeIfAbsent(clazz, (it) -> getFieldsDirectly(it, true, true));
    }

    public static List<Field> getFieldsDirectly(Class<?> clazz, boolean distinctField, boolean withSuperClassFields) throws SecurityException {
        List<Field> allFields = new ArrayList<>();
        Map<String, Void> distinctFieldMap = new HashMap<>(0);
        for (Class<?> searchType = clazz; searchType != null; searchType = withSuperClassFields ? searchType.getSuperclass() : null) {
            if (distinctField) {
                for (Field field : searchType.getDeclaredFields()) {
                    if (!distinctFieldMap.containsKey(field.getName())) {
                        allFields.add(field);
                        distinctFieldMap.put(field.getName(), null);
                    }
                }
            } else {
                allFields.addAll(Arrays.asList(searchType.getDeclaredFields()));
            }
        }
        return allFields;
    }

}
