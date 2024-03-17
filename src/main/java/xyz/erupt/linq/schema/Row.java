package xyz.erupt.linq.schema;

import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaReflect;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.util.ColumnReflects;

import java.math.BigDecimal;
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
        for (Column column : this.keySet()) {
            if (alias.equals(column.getAlias())) {
                return super.get(column);
            }
        }
        return null;
    }

    public <T, R> R get(SFunction<T, R> alias) {
        LambdaInfo lambdaInfo = LambdaReflect.getInfo(alias);
        Object val = this.get(lambdaInfo.getField());
        if (val instanceof BigDecimal) {
            return (R) ColumnReflects.bigDecimalConvert((BigDecimal) val, lambdaInfo.getFieldClazz().getType());
        } else {
            return (R) val;
        }
    }

}
