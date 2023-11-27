package xyz.erupt.eql.query;

import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;

public class DefaultQuery extends Query {
    @Override
    public void dql(Dql dql) {
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
//                joinSchema.
            }
        }
    }

}
