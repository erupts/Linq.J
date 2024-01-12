package xyz.erupt.eql.schema;

import xyz.erupt.eql.util.Columns;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Column<T> implements Cloneable {

    private Class<T> table;

    private Class<?> fieldType;

    private String field;

    private String alias;

    //列值转换处理逻辑
    private Function<Object, Object> valueConvertFun;

    //The handler functions listed in the grouping scenario
    private Function<List<Map<Column<?>, ?>>, Object> groupByFun;

    public Column() {
    }

    public Column(Class<T> table, Class<?> fieldType, String field, String alias) {
        this.table = table;
        this.field = field;
        this.fieldType = fieldType;
        this.alias = alias;
    }

    // Get the original column information
    public Column<?> getRawColumn() {
        Column<?> column = new Column<>(this.table, this.fieldType, this.field, this.field);
        column.setGroupByFun(this.getGroupByFun());
        column.setValueConvertFun(this.getValueConvertFun());
        return column;
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

    @Override
    public String toString() {
        return table.getSimpleName() + "." + field + " (" + alias + ")";
    }


}
