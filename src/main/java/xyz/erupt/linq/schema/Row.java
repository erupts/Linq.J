package xyz.erupt.linq.schema;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.util.RowUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Row extends HashMap<Column, Object> {

    // Cache for alias to Column mapping to avoid repeated iteration
    private transient HashMap<String, Column> aliasCache = null;

    public Row(int initialCapacity) {
        super(initialCapacity);
    }

    public Row(Row row) {
        super(row);
        if (row.aliasCache != null) {
            this.aliasCache = new HashMap<>(row.aliasCache);
        }
    }

    public Row() {
        super();
    }

    @Override
    public Object put(Column column, Object value) {
        // Update alias cache when adding new column (lazy initialization)
        if (aliasCache != null) {
            aliasCache.put(column.getAlias(), column);
        }
        return super.put(column, value);
    }

    public Object get(Column column) {
        return super.get(column);
    }

    public Object get(String alias) {
        // Build alias cache on first access if not exists
        if (aliasCache == null) {
            aliasCache = new HashMap<>(this.size());
            // Use entrySet for better performance
            for (Map.Entry<Column, Object> entry : this.entrySet()) {
                aliasCache.put(entry.getKey().getAlias(), entry.getKey());
            }
        }
        Column column = aliasCache.get(alias);
        return column != null ? super.get(column) : null;
    }

    public <T, R> R get(SFunction<T, R> alias) {
        LambdaInfo lambdaInfo = LambdaSee.info(alias);
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
