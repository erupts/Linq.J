package xyz.erupt.linq.schema;

import xyz.erupt.linq.consts.JoinExchange;
import xyz.erupt.linq.consts.JoinMethod;
import xyz.erupt.linq.lambda.LambdaReflect;
import xyz.erupt.linq.lambda.SFunction;

import java.util.Collection;
import java.util.function.BiFunction;

public class JoinSchema<T> {

    private final JoinMethod joinMethod;

    private final JoinExchange joinExchange;

    private final Collection<T> target;

    private final Class<T> clazz;

    private BiFunction<T, Row, Boolean> on;

    private SFunction<T, ?> lon;


    private SFunction<?, ?> ron;


    public JoinSchema(JoinMethod joinMethod, Collection<T> target, SFunction<T, ?> lon, SFunction<?, ?> ron) {
        this.joinMethod = joinMethod;
        this.target = target;
        this.lon = lon;
        this.ron = ron;
        this.clazz = (Class<T>) LambdaReflect.getInfo(lon).getClazz();
        this.joinExchange = JoinExchange.HASH;
    }

    public JoinSchema(JoinMethod joinMethod, Collection<T> target, BiFunction<T, Row, Boolean> on) {
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

    public BiFunction<T, Row, Boolean> getOn() {
        return on;
    }

    public void setOn(BiFunction<T, Row, Boolean> on) {
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
