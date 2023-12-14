package xyz.erupt.eql.grammar;

import xyz.erupt.eql.Linq;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.lambda.SFunction;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.JoinSchema;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public interface Join {

    <T> Linq join(JoinSchema<T> joinSchema);


    <T, S> Linq join(JoinMethod joinMethod, Collection<T> target, SFunction<T, Object> onL, SFunction<S, Object> onR) ;



    default <T> Linq join(JoinMethod joinMethod, Collection<T> target, BiFunction<T, Map<Column<?>, ?>, Boolean> on) {
        return join(new JoinSchema<>(joinMethod, target, on));
    }

//    default <T, S> Linq join(JoinMethod joinMethod, Collection<T> target, SFunction<T, Object> onL, SFunction<S, Object> onR) {
//        Column<T> l = Columns.fromLambda(onL);
//        Column<S> r = Columns.fromLambda(onR);
//        return join(new JoinSchema<>(joinMethod, target, (t1, t2) -> {
//            Object lv = ReflectUtil.getFieldValue(t1, l.getField());
//            Object rv = t2.get(r);
//            if (null == lv && null == rv) {
//                return true;
//            } else if (null == lv || null == rv) {
//                return false;
//            } else {
//                return lv.toString().equals(rv.toString());
//            }
//        }));
//    }

    default <L, R> Linq leftJoin(Collection<L> t, SFunction<L, Object> lon, SFunction<R, Object> ron) {
        return this.join(JoinMethod.LEFT, t, lon, ron);
    }

}
