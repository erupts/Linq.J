package xyz.erupt.eql.schema;

import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.util.ReflectUtil;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public class JoinSchema<T> {

    private final JoinMethod joinMethod;

    private final Collection<T> target;

    private final Class<T> clazz;

    private final BiFunction<T, Map<Column<?>, ?>, Boolean> on;

    public JoinSchema(JoinMethod joinMethod, Collection<T> target, BiFunction<T, Map<Column<?>, ?>, Boolean> on) {
        this.joinMethod = joinMethod;
        this.target = target;
        this.on = on;
        this.clazz = (Class<T>) ReflectUtil.getActualType(target, 0);
    }

    public JoinMethod getJoinMethod() {
        return joinMethod;
    }

    public Collection<T> getTarget() {
        return target;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public BiFunction<T, Map<Column<?>, ?>, Boolean> getOn() {
        return on;
    }
}
