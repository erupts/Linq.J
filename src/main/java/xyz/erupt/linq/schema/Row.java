package xyz.erupt.linq.schema;

import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.util.RowUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Optimized Row implementation using pure array storage - no HashMap overhead.
 * This significantly reduces memory usage for large datasets.
 */
public class Row extends AbstractMap<Column, Object> {

    // Array-based storage - no HashMap overhead
    private Column[] columns;
    private Object[] values;
    private int size;

    // Alias cache using arrays for compact storage (only created when needed)
    private String[] aliasKeys;
    private Column[] aliasValues;
    private int aliasCacheSize;

    public Row(int initialCapacity) {
        // Use exact capacity to avoid waste
        this.columns = new Column[initialCapacity];
        this.values = new Object[initialCapacity];
        this.size = 0;
    }

    public Row(Row row) {
        // Copy arrays directly - very efficient
        this.size = row.size;
        this.columns = Arrays.copyOf(row.columns, row.size);
        this.values = Arrays.copyOf(row.values, row.size);
        // Copy alias cache if exists
        if (row.aliasKeys != null) {
            this.aliasKeys = Arrays.copyOf(row.aliasKeys, row.aliasCacheSize);
            this.aliasValues = Arrays.copyOf(row.aliasValues, row.aliasCacheSize);
            this.aliasCacheSize = row.aliasCacheSize;
        }
    }

    public Row() {
        this(8); // Default small capacity
    }

    @Override
    public Object put(Column column, Object value) {
        // Check if column already exists (identity check first for performance, then equals)
        for (int i = 0; i < size; i++) {
            if (columns[i] == column || columns[i].equals(column)) {
                Object oldValue = values[i];
                values[i] = value;
                return oldValue;
            }
        }

        // Add new entry
        putDirect(column, value);
        return null;
    }
    
    /**
     * Direct put without duplicate check - for performance optimization.
     * Use this when you know the column doesn't exist (e.g., in listToTable).
     * This avoids O(n) linear search overhead.
     */
    public void putDirect(Column column, Object value) {
        // Grow array if needed
        if (size >= columns.length) {
            int newCapacity = size + (size >> 1) + 1; // 1.5x growth
            columns = Arrays.copyOf(columns, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }

        columns[size] = column;
        values[size] = value;
        size++;

        // Update alias cache if exists
        if (aliasKeys != null) {
            updateAliasCache(column);
        }
    }

    @Override
    public Object get(Object key) {
        if (key instanceof Column) {
            return get((Column) key);
        }
        return null;
    }

    public Object get(Column column) {
        // Linear search - fast for small arrays, acceptable for larger ones
        // Use identity check first for performance, then equals for compatibility
        for (int i = 0; i < size; i++) {
            if (columns[i] == column || columns[i].equals(column)) {
                return values[i];
            }
        }
        return null;
    }

    public Object get(String alias) {
        // Build alias cache on first access if not exists
        if (aliasKeys == null) {
            buildAliasCache();
        }

        // Linear search in alias cache (usually small)
        for (int i = 0; i < aliasCacheSize; i++) {
            if (aliasKeys[i].equals(alias)) {
                return get(aliasValues[i]);
            }
        }
        return null;
    }

    private void buildAliasCache() {
        aliasKeys = new String[size];
        aliasValues = new Column[size];
        aliasCacheSize = 0;
        for (int i = 0; i < size; i++) {
            aliasKeys[aliasCacheSize] = columns[i].getAlias();
            aliasValues[aliasCacheSize] = columns[i];
            aliasCacheSize++;
        }
    }

    private void updateAliasCache(Column column) {
        // Grow alias cache if needed
        if (aliasCacheSize >= aliasKeys.length) {
            int newCapacity = aliasCacheSize + (aliasCacheSize >> 1) + 1;
            aliasKeys = Arrays.copyOf(aliasKeys, newCapacity);
            aliasValues = Arrays.copyOf(aliasValues, newCapacity);
        }
        aliasKeys[aliasCacheSize] = column.getAlias();
        aliasValues[aliasCacheSize] = column;
        aliasCacheSize++;
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

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Column) {
            Column column = (Column) key;
            for (int i = 0; i < size; i++) {
                // Use equals for compatibility, but prefer identity check for performance
                if (columns[i] == column || columns[i].equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map)) return false;
        Map<?, ?> other = (Map<?, ?>) o;
        if (size != other.size()) return false;
        
        // Compare all entries - for each key in this map, check if other has same key-value
        for (int i = 0; i < size; i++) {
            Column key = columns[i];
            Object value = values[i];
            
            // Check if other map contains this key
            if (!other.containsKey(key)) {
                return false;
            }
            
            // Compare values
            Object otherValue = other.get(key);
            if (value == null) {
                if (otherValue != null) {
                    return false;
                }
            } else {
                if (!value.equals(otherValue)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        // Standard Map hashCode: sum of (key.hashCode() ^ value.hashCode()) for all entries
        int result = 0;
        for (int i = 0; i < size; i++) {
            Column key = columns[i];
            Object value = values[i];
            result += (key == null ? 0 : key.hashCode()) ^ 
                      (value == null ? 0 : value.hashCode());
        }
        return result;
    }

    @Override
    public Set<Entry<Column, Object>> entrySet() {
        return new AbstractSet<Entry<Column, Object>>() {
            @Override
            public Iterator<Entry<Column, Object>> iterator() {
                return new Iterator<Entry<Column, Object>>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public Entry<Column, Object> next() {
                        if (index >= size) {
                            throw new NoSuchElementException();
                        }
                        final int i = index++;
                        return new Entry<Column, Object>() {
                            @Override
                            public Column getKey() {
                                return columns[i];
                            }

                            @Override
                            public Object getValue() {
                                return values[i];
                            }

                            @Override
                            public Object setValue(Object value) {
                                Object oldValue = values[i];
                                values[i] = value;
                                return oldValue;
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return Row.this.size;
            }
        };
    }

    @Override
    public void clear() {
        // Clear arrays to help GC
        Arrays.fill(columns, 0, size, null);
        Arrays.fill(values, 0, size, null);
        size = 0;
        aliasKeys = null;
        aliasValues = null;
        aliasCacheSize = 0;
    }

    @Override
    public void putAll(Map<? extends Column, ? extends Object> m) {
        if (m instanceof Row) {
            Row otherRow = (Row) m;
            // Direct array copy for efficiency - avoid put() overhead
            int otherSize = otherRow.size;
            int newSize = size + otherSize;
            
            // Grow arrays if needed
            if (newSize > columns.length) {
                int newCapacity = Math.max(newSize, columns.length + (columns.length >> 1) + 1);
                columns = Arrays.copyOf(columns, newCapacity);
                values = Arrays.copyOf(values, newCapacity);
            }
            
            // Copy all entries directly
            for (int i = 0; i < otherSize; i++) {
                Column col = otherRow.columns[i];
                // Check if already exists (using identity first, then equals)
                boolean exists = false;
                for (int j = 0; j < size; j++) {
                    if (columns[j] == col || columns[j].equals(col)) {
                        values[j] = otherRow.values[i];
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    columns[size] = col;
                    values[size] = otherRow.values[i];
                    size++;
                }
            }
            
            // Rebuild alias cache if exists
            if (aliasKeys != null) {
                buildAliasCache();
            }
        } else {
            // Fallback for other Map types
            for (Map.Entry<? extends Column, ? extends Object> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Object remove(Object key) {
        if (key instanceof Column) {
            Column column = (Column) key;
            for (int i = 0; i < size; i++) {
                if (columns[i] == column || columns[i].equals(column)) {
                    Object oldValue = values[i];
                    // Shift remaining elements
                    System.arraycopy(columns, i + 1, columns, i, size - i - 1);
                    System.arraycopy(values, i + 1, values, i, size - i - 1);
                    columns[size - 1] = null;
                    values[size - 1] = null;
                    size--;

                    // Rebuild alias cache if exists
                    if (aliasKeys != null) {
                        buildAliasCache();
                    }
                    return oldValue;
                }
            }
        }
        return null;
    }
}

