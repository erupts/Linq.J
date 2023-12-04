package xyz.erupt.eql.query;

import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;

import java.util.*;
import java.util.function.Function;

public class DefaultQuery extends Query {
    @Override
    public <T> Collection<T> dql(Dql dql, Class<T> target) {
        List<Map<Column<?>, ?>> table = LambdaInfo.objectToLambdaInfos(dql.getSource());
        //join process
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
                for (Map<Column<?>, ?> map : table) {
                    for (Object t : joinSchema.getTarget()) {
                        //是否关联
                        if (joinSchema.getOn().apply(null, map)) {
                            //关联方式
                            switch (joinSchema.getJoinMethod()) {
                                case LEFT:

                                    break;
                                case RIGHT:

                                    break;
                                case INNER:

                                    break;
                                case FULL:

                                    break;
                            }
                        }
                    }
                }
            }
        }
        // condition process
        for (Map<Column<?>, ?> map : table) {
            List<Map<Column<?>, ?>> r = new ArrayList<>();
            for (Function<Map<Column<?>, ?>, Boolean> condition : dql.getConditions()) {
                if (condition.apply(map)) {
                    r.add(map);
                }
            }
            table = r;
        }
        // group process
        for (Column<?> groupBy : dql.getGroupBys()) {
            //联动 select
        }

        List<T> result = new ArrayList<>();
        // select process
        for (Map<Column<?>, ?> t : table) {
            Map<Column<?>, Object> map = new HashMap<>();
            for (Column<?> column : t.keySet()) {
                for (Column<?> dqlColumn : dql.getColumns()) {
                    if (target == Map.class) {

                    }else{
                        if (dqlColumn == column) {
                            map.put(dqlColumn, t.get(dqlColumn));
                        }
                    }
                }
            }
//            result.add(map);
        }
        return result;
    }

}
