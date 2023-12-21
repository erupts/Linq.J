package xyz.erupt.eql.schema;

import java.util.Objects;

public class Column<T> {

    private Class<T> table;

    private Class<?> fieldType;

    private String field;

    private String alias;

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
}
