package xyz.erupt.linq.schema;

import xyz.erupt.linq.consts.JoinStrategy;
import xyz.erupt.linq.consts.JoinType;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;

import java.util.List;

public class JoinSchema<T> {

    private final JoinType joinType;

    private final JoinStrategy joinStrategy;

    private final List<T> target;

    private final Class<T> clazz;

    // join key on the target (joined-in) table
    private final SFunction<T, ?> targetOn;

    // join key on the source (driving) table
    private final SFunction<?, ?> sourceOn;

    @SuppressWarnings("unchecked")
    public JoinSchema(JoinType joinType, List<T> target, SFunction<T, ?> targetOn, SFunction<?, ?> sourceOn) {
        this.joinType = joinType;
        this.target = target;
        this.targetOn = targetOn;
        this.sourceOn = sourceOn;
        this.clazz = (Class<T>) LambdaSee.info(targetOn).getClazz();
        this.joinStrategy = JoinStrategy.HASH;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public JoinStrategy getJoinStrategy() {
        return joinStrategy;
    }

    public List<T> getTarget() {
        return target;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public SFunction<T, ?> getTargetOn() {
        return targetOn;
    }

    public SFunction<?, ?> getSourceOn() {
        return sourceOn;
    }

}
