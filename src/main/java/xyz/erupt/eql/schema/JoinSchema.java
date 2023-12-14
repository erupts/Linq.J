package xyz.erupt.eql.schema;

import xyz.erupt.eql.consts.JoinExchange;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.lambda.LambdaReflect;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.util.ReflectUtil;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public class JoinSchema<T> {

    private final JoinMethod joinMethod;

    private final JoinExchange joinExchange;

    private final Collection<T> target;

    private final Class<T> clazz;

    private BiFunction<T, Map<Column<?>, ?>, Boolean> on;

    private SFunction<T, ?> lon;


    private SFunction<?, ?> ron;


    public JoinSchema(JoinMethod joinMethod, Collection<T> target, SFunction<T, ?> lon, SFunction<?, ?> ron) {
        this.joinMethod = joinMethod;
        this.target = target;
        this.lon = lon;
        this.ron = ron;
        this.clazz = LambdaReflect.getInfo(lon).getClazz();
        this.joinExchange = JoinExchange.HASH;
    }

    public JoinSchema(JoinMethod joinMethod, Collection<T> target, BiFunction<T, Map<Column<?>, ?>, Boolean> on) {
        this.joinMethod = joinMethod;
        this.target = target;
        this.on = on;
        this.clazz = (Class<T>) target.getClass().getGenericSuperclass().getClass();
        this.joinExchange = JoinExchange.NESTED_LOOP;
    }

    public JoinMethod getJoinMethod() {
        return joinMethod;
    }

    public JoinExchange getJoinExchange() {
        return joinExchange;
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

    public void setOn(BiFunction<T, Map<Column<?>, ?>, Boolean> on) {
        this.on = on;
    }

    public SFunction<T, ?> getLon() {
        return lon;
    }

    public void setLon(SFunction<T, ?> lon) {
        this.lon = lon;
    }

    public SFunction<?, ?> getRon() {
        return ron;
    }

    public void setRon(SFunction<?, ?> ron) {
        this.ron = ron;
    }
}
