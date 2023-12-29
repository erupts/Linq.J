package xyz.erupt.eql.schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Column<T> {

    private Class<T> table;

    private Class<?> fieldType;

    private String field;

    private String alias;

    //列值转换处理逻辑
    private Function<Object, Object> valueConvertFun;

    //列在分组场景的处理函数
    private Function<List<Map<Column<?>, ?>>, Object> groupByFun;

    public Column() {
    }

    public Column(Class<T> table, Class<?> fieldType, String field, String alias) {
        this.table = table;
        this.field = field;
        this.fieldType = fieldType;
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column<?> column = (Column<?>) o;
        return Objects.equals(table.getSimpleName(), column.table.getSimpleName()) && Objects.equals(field, column.field) && Objects.equals(alias, column.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, field, alias);
    }

    public Class<?> getTable() {
        return table;
    }

    public void setTable(Class<T> table) {
        this.table = table;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    public Column(Function<Object, Object> valueConvertFun, Function<List<Map<Column<?>, ?>>, Object> groupByFun) {
        this.valueConvertFun = valueConvertFun;
        this.groupByFun = groupByFun;
    }

    public Function<List<Map<Column<?>, ?>>, Object> getGroupByFun() {
        return groupByFun;
    }

    public void setGroupByFun(Function<List<Map<Column<?>, ?>>, Object> groupByFun) {
        this.groupByFun = groupByFun;
    }

    public Function<Object, Object> getValueConvertFun() {
        return valueConvertFun;
    }

    public void setValueConvertFun(Function<Object, Object> valueConvertFun) {
        this.valueConvertFun = valueConvertFun;
    }
}
