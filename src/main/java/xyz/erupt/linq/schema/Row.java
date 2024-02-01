package xyz.erupt.linq.schema;

import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.util.Columns;

import java.util.HashMap;

public class Row extends HashMap<Column, Object> {

    public Row(int initialCapacity) {
        super(initialCapacity);
    }

    public Row(Row row) {
        super(row);
    }

    public Row() {
        super();
    }

    public Object get(Column column) {
        return super.get(column);
    }

    public Object get(String alias) {
        return super.get(alias);
    }

    public <T, R> Object get(SFunction<T, R> alias) {
        return super.get(Columns.fromLambda(alias));
    }

}
