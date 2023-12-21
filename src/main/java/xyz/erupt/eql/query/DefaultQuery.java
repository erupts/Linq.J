package xyz.erupt.eql.query;

import xyz.erupt.eql.consts.JoinExchange;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;
import xyz.erupt.eql.util.Columns;

import java.util.*;
import java.util.function.Function;

public class DefaultQuery extends Query {

    @Override
    public <T> Collection<T> dql(Dql dql, Class<T> target) {
        List<Map<Column<?>, Object>> table = LambdaInfo.objectToLambdaInfos(dql.getSource());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
                Column<?> lon = Columns.fromLambda(joinSchema.getLon());
                Column<?> ron = Columns.fromLambda(joinSchema.getRon());
                if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                    switch (joinSchema.getJoinMethod()) {
                        case LEFT:
                        case INNER:
                        case FULL:
                            Map<Object, Map<Column<?>, ?>> rightMap = new HashMap<>();
                            List<Map<Column<?>, Object>> targetList = LambdaInfo.objectToLambdaInfos(joinSchema.getTarget());
                            for (Map<Column<?>, Object> tm : targetList) {
                                rightMap.put(tm.get(lon), tm);
                            }
                            for (Map<Column<?>, Object> map : table) {
                                if (rightMap.containsKey(map.get(ron))) {
                                    Map<Column<?>, ?> m = rightMap.get(map.get(ron));
                                    map.putAll(m);
                                }
                            }
                            if (JoinMethod.INNER == joinSchema.getJoinMethod()) {
                                table.removeIf(it -> !it.containsKey(lon));
                            } else if (JoinMethod.FULL == joinSchema.getJoinMethod()) {
                                //TODO
                            }
                            break;
                        case RIGHT:
                            Map<Object, Map<Column<?>, ?>> leftMap = new HashMap<>();
                            for (Map<Column<?>, ?> tm : table) {
                                leftMap.put(tm.get(ron), tm);
                            }
                            table.clear();
                            for (Map<Column<?>, Object> map : LambdaInfo.objectToLambdaInfos(joinSchema.getTarget())) {
                                table.add(map);
                                if (leftMap.containsKey(map.get(lon))) {
                                    Map<Column<?>, ?> m = leftMap.get(map.get(lon));
                                    map.putAll(m);
                                }
                            }
                            break;
                    }
                } else {
                    throw new EqlException(joinSchema.getJoinExchange().name() + " is not supported yet");
                }
            }
        }

        // condition process
        table.removeIf(it -> {
            boolean remove = false;
            for (Function<Map<Column<?>, ?>, Boolean> condition : dql.getConditions()) {
                if (!condition.apply(it)) remove = true;
            }
            return remove;
        });

        System.out.println(table);

        // group process
        Map<List<Column<?>>, List<Map<Column<?>, ?>>> abc;
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

                    } else {
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

    private boolean eq(Object a, Object b) {
        if (null == a || null == b) {
            return false;
        } else {
            return a.equals(b);
        }
    }

}
