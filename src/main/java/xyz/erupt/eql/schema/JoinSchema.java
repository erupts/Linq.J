package xyz.erupt.eql.schema;

import xyz.erupt.eql.consts.JoinMethod;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public class JoinSchema<T> {

    private final JoinMethod joinMethod;

    private final Collection<T> target;

    private final Class<T> clazz;

    private BiFunction<Map<Column<T>, ?>, Map<Column<?>, ?>, Boolean> on;

    public JoinSchema(JoinMethod joinMethod, Collection<T> target, BiFunction<Map<Column<T>, ?>, Map<Column<?>, ?>, Boolean> on) {
        this.joinMethod = joinMethod;
        this.target = target;
        this.on = on;
        ParameterizedType parameterizedType = (ParameterizedType) target.getClass().getGenericSuperclass();
        this.clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0].getClass();
    }

    public JoinMethod getJoinMethod() {
        return joinMethod;
    }

    public Collection<T> getTarget() {
        return target;
    }

    public BiFunction<Map<Column<T>, ?>, Map<Column<?>, ?>, Boolean> getOn() {
        return on;
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
