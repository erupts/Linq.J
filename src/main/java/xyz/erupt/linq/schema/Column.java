package xyz.erupt.linq.schema;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class Column {

    private Class<?> table;

    private Class<?> fieldType;

    private String field;

    private String alias;

    // Column unfold process
    private Supplier<List<Column>> unfold;

    // Column value convert handler
    private Function<Row, ?> valueConvertFun;

    // The handler functions listed in the grouping scenario
    private Function<List<Row>, Object> groupByFun;

    public Column() {
    }

    public Column(Class<?> table, Class<?> fieldType, String field, String alias) {
        this.table = table;
        this.field = field;
        this.fieldType = fieldType;
        this.alias = alias;
    }

    // Get the original column information
    public Column getRawColumn() {
        Column column = new Column(this.table, this.fieldType, this.field, this.field);
        column.setGroupByFun(this.getGroupByFun());
        column.setValueConvertFun(this.getValueConvertFun());
        column.setUnfold(this.unfold);
        return column;
    }

    public Class<?> getTable() {
        return table;
    }

    public void setTable(Class<?> table) {
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


    public Function<List<Row>, Object> getGroupByFun() {
        return groupByFun;
    }

    public void setGroupByFun(Function<List<Row>, Object> groupByFun) {
        this.groupByFun = groupByFun;
    }

    public Function<Row, ?> getValueConvertFun() {
        return valueConvertFun;
    }

    public void setValueConvertFun(Function<Row, ?> valueConvertFun) {
        this.valueConvertFun = valueConvertFun;
    }

    public Supplier<List<Column>> getUnfold() {
        return unfold;
    }

    public void setUnfold(Supplier<List<Column>> unfold) {
        this.unfold = unfold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(table.getSimpleName(), column.table.getSimpleName()) && Objects.equals(field, column.field) && Objects.equals(alias, column.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, field, alias);
    }

    @Override
    public String toString() {
        return table.getSimpleName() + "." + field + "(" + alias + ")";
    }


}