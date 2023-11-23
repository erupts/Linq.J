package xyz.erupt.eql.schema;

import xyz.erupt.eql.consts.JoinMethod;

import java.util.Collection;
import java.util.function.BiFunction;

public class JoinSchema<S, T> {

    private JoinMethod joinMethod;

    private Collection<S> source;

    private Collection<T> target;

    private BiFunction<S, T, Boolean> on;

    public JoinSchema(JoinMethod joinMethod, Collection<S> source, Collection<T> target, BiFunction<S, T, Boolean> on) {
        this.joinMethod = joinMethod;
        this.source = source;
        this.target = target;
        this.on = on;
    }

    public JoinMethod getJoinMethod() {
        return joinMethod;
    }

    public void setJoinMethod(JoinMethod joinMethod) {
        this.joinMethod = joinMethod;
    }

    public Collection<S> getSource() {
        return source;
    }

    public void setSource(Collection<S> source) {
        this.source = source;
    }

    public Collection<T> getTarget() {
        return target;
    }

    public void setTarget(Collection<T> target) {
        this.target = target;
    }

    public BiFunction<S, T, Boolean> getOn() {
        return on;
    }

    public void setOn(BiFunction<S, T, Boolean> on) {
        this.on = on;
    }
}
