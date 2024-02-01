package xyz.erupt.linq.schema;

import xyz.erupt.linq.lambda.SFunction;

import java.util.HashMap;

public class Row extends HashMap<Column, Object> {

    public Object get(String alias) {

        return super.get(alias);
    }

    public <T, R> Object get(SFunction<T, R> alias) {
        return super.get(alias);
    }
}
