package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.consts.JoinType;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.JoinSchema;

import java.util.List;

public interface Join {

    <T> Linq join(JoinSchema<T> joinSchema);

    /**
     * SQL: {@code JOIN target ON target.targetOn = source.sourceOn}
     *
     * <pre>{@code Linq.from(emps).innerJoin(depts, Dept::getId, Emp::getDeptId) }</pre>
     */
    <T, S> Linq join(JoinType joinType, List<T> target, SFunction<T, ?> targetOn, SFunction<S, ?> sourceOn);

    default <T, S> Linq innerJoin(List<T> target, SFunction<T, ?> targetOn, SFunction<S, ?> sourceOn) {
        return this.join(JoinType.INNER, target, targetOn, sourceOn);
    }

    default <T, S> Linq leftJoin(List<T> target, SFunction<T, ?> targetOn, SFunction<S, ?> sourceOn) {
        return this.join(JoinType.LEFT, target, targetOn, sourceOn);
    }

    default <T, S> Linq rightJoin(List<T> target, SFunction<T, ?> targetOn, SFunction<S, ?> sourceOn) {
        return this.join(JoinType.RIGHT, target, targetOn, sourceOn);
    }

    default <T, S> Linq fullJoin(List<T> target, SFunction<T, ?> targetOn, SFunction<S, ?> sourceOn) {
        return this.join(JoinType.FULL, target, targetOn, sourceOn);
    }

}
