package xyz.erupt.eql.query;

import xyz.erupt.eql.schema.Dql;

import java.util.Collection;

public abstract class Query {


    public abstract <T> Collection<T> dql(Dql dql, Class<T> target);

}
