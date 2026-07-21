package xyz.erupt.linq.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Builds and caches fast property getters and setters backed by {@link LambdaMetafactory}-generated
 * lambdas. Once JIT-compiled these approach direct-call speed (~3x faster than reflective
 * {@code Field.get}/{@code Field.set}). Only public accessor methods can be bound this way —
 * {@code LambdaMetafactory} refuses field-access handles — so when no suitable method exists the
 * builder returns {@code null} and the caller falls back to reflection.
 *
 * <p>JDK 8 compatible: uses only {@code MethodHandles.lookup()} + {@code LambdaMetafactory}.
 */
public final class Accessors {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Sentinel meaning "already tried, no fast getter available — use reflection".
    private static final Function<Object, Object> NONE = o -> o;

    private static final Map<Class<?>, Map<String, Function<Object, Object>>> GETTER_CACHE = new ConcurrentHashMap<>();

    // Sentinel meaning "already tried, no fast setter available — use reflection".
    private static final BiConsumer<Object, Object> NONE_SETTER = (o, v) -> {
    };

    private static final Map<Class<?>, Map<String, BiConsumer<Object, Object>>> SETTER_CACHE = new ConcurrentHashMap<>();

    private Accessors() {
    }

    /**
     * @return a JIT-inlinable getter for the given field, or {@code null} if one could not be
     * built (caller should fall back to reflection).
     */
    public static Function<Object, Object> getter(Class<?> clazz, String field, Class<?> fieldType) {
        Function<Object, Object> fn = GETTER_CACHE
                .computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(field, k -> {
                    Function<Object, Object> built = build(clazz, k, fieldType);
                    return built == null ? NONE : built;
                });
        return fn == NONE ? null : fn;
    }

    private static Function<Object, Object> build(Class<?> clazz, String field, Class<?> fieldType) {
        // LambdaMetafactory can only reach a public method on a public class from this lookup.
        if (!Modifier.isPublic(clazz.getModifiers())) return null;
        Method getter = findGetter(clazz, field, fieldType);
        if (getter == null || !Modifier.isPublic(getter.getModifiers())) return null;
        try {
            MethodHandle h = LOOKUP.unreflect(getter);
            CallSite cs = LambdaMetafactory.metafactory(LOOKUP, "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    h, h.type());
            @SuppressWarnings("unchecked")
            Function<Object, Object> fn = (Function<Object, Object>) cs.getTarget().invoke();
            return fn;
        } catch (Throwable t) {
            // Any linkage/access failure -> signal fallback rather than break the query.
            return null;
        }
    }

    /**
     * @return a JIT-inlinable setter bound to {@code setXxx(fieldType)} for the given field, or
     * {@code null} if one could not be built (caller should fall back to reflection).
     */
    public static BiConsumer<Object, Object> setter(Class<?> clazz, String field, Class<?> fieldType) {
        BiConsumer<Object, Object> fn = SETTER_CACHE
                .computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(field, k -> {
                    BiConsumer<Object, Object> built = buildSetter(clazz, k, fieldType);
                    return built == null ? NONE_SETTER : built;
                });
        return fn == NONE_SETTER ? null : fn;
    }

    private static BiConsumer<Object, Object> buildSetter(Class<?> clazz, String field, Class<?> fieldType) {
        if (!Modifier.isPublic(clazz.getModifiers())) return null;
        String cap = Character.toUpperCase(field.charAt(0)) + field.substring(1);
        Method setter;
        try {
            setter = clazz.getMethod("set" + cap, fieldType);
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (!Modifier.isPublic(setter.getModifiers())) return null;
        try {
            MethodHandle h = LOOKUP.unreflect(setter);
            // wrap() lets a primitive setter parameter be reached through its box; the void SAM
            // shape drops a fluent setter's return value.
            CallSite cs = LambdaMetafactory.metafactory(LOOKUP, "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    h, h.type().wrap().changeReturnType(void.class));
            @SuppressWarnings("unchecked")
            BiConsumer<Object, Object> fn = (BiConsumer<Object, Object>) cs.getTarget().invoke();
            return fn;
        } catch (Throwable t) {
            // Any linkage/access failure -> signal fallback rather than break the query.
            return null;
        }
    }

    private static Method findGetter(Class<?> clazz, String field, Class<?> fieldType) {
        String cap = Character.toUpperCase(field.charAt(0)) + field.substring(1);
        Method m = tryMethod(clazz, "get" + cap);
        if (m == null && (fieldType == boolean.class || fieldType == Boolean.class)) {
            m = tryMethod(clazz, "is" + cap);
        }
        if (m == null) {
            m = tryMethod(clazz, field); // record-style accessor: name()
        }
        return m;
    }

    private static Method tryMethod(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
