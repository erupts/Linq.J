package xyz.erupt.linq.schema;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaReflect;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.util.RowUtil;

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

    @Override
    public Object put(Column column, Object value) {
        return super.put(column, value);
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
        LambdaInfo lambdaInfo = LambdaReflect.info(alias);
        Object val = this.get(lambdaInfo.getField());
        try {
            if (val instanceof BigDecimal) {
                return (R) RowUtil.bigDecimalConvert((BigDecimal) val, lambdaInfo.getClazz().getDeclaredField(lambdaInfo.getField()).getType());
            } else {
                return (R) val;
            }
        } catch (Exception e) {
            throw new LinqException(e);
        }
    }

}
