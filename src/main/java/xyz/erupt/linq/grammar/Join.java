package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.consts.JoinMethod;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.JoinSchema;
import xyz.erupt.linq.schema.Row;

import java.util.Collection;
import java.util.function.BiFunction;

public interface Join {

    <T> Linq join(JoinSchema<T> joinSchema);


    <T, S> Linq join(JoinMethod joinMethod, Collection<T> target, SFunction<T, Object> onL, SFunction<S, Object> onR);


    default <T> Linq join(JoinMethod joinMethod, Collection<T> target, BiFunction<T, Row, Boolean> on) {
        return join(new JoinSchema<>(joinMethod, target, on));
    }

    default <L, R> Linq innerJoin(Collection<L> t, SFunction<L, Object> lon, SFunction<R, Object> ron) {
        return this.join(JoinMethod.INNER, t, lon, ron);
    }

    default <L, R> Linq leftJoin(Collection<L> t, SFunction<L, Object> lon, SFunction<R, Object> ron) {
        return this.join(JoinMethod.LEFT, t, lon, ron);
    }

    default <L, R> Linq rightJoin(Collection<L> t, SFunction<L, Object> lon, SFunction<R, Object> ron) {
        return this.join(JoinMethod.RIGHT, t, lon, ron);
    }

    default <L, R> Linq fullJoin(Collection<L> t, SFunction<L, Object> lon, SFunction<R, Object> ron) {
        return this.join(JoinMethod.FULL, t, lon, ron);
    }

}
