package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.consts.JoinMethod;
import xyz.erupt.linq.fun.SFunction;

import java.util.Collection;
import java.util.function.BiFunction;

public interface Join<Source> {

    <T1, T2> Linq<Source> join(JoinMethod joinMethod, Class<T1> t1, Collection<T2> t2, BiFunction<T1, T2, Boolean> on);

    <T> Linq<Source> join(JoinMethod joinMethod, Collection<T> t, BiFunction<T, Source, Boolean> on);


    default <T> Linq<Source> join(JoinMethod joinMethod, Collection<T> target, SFunction<T, Object> lon, SFunction<Source, Object> ron) {
//        LambdaReflect.getFieldName(lon).get;
//        LambdaReflect.getFieldName(ron);
        return this.join(joinMethod, target, (t1, t2) -> {
            return true;
        });
    }

    default <T> Linq<Source> leftJoin(Collection<T> t, BiFunction<T, Source, Boolean> on) {
        return this.join(JoinMethod.LEFT, t, on);
    }

    default <T> Linq<Source> leftJoin(Collection<T> t, SFunction<T, Object> lon, SFunction<Source, Object> ron) {
        return this.join(JoinMethod.RIGHT, t, lon, ron);
    }

}
