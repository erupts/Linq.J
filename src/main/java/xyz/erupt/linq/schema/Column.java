package xyz.erupt.linq.schema;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class Column {

    private Class<?> table;

    private String field;

    private String alias;

    // Column unfold process
    private Supplier<List<Column>> unfold;

    // Column value convert handler
    private Function<Row, ?> rowConvert;

    // The handler functions listed in the grouping scenario
    private Function<List<Row>, Object> groupByFun;

    public Column() {
    }

    public Column(Class<?> table, String field, String alias) {
        this.table = table;
        this.field = field;
        this.alias = alias;
    }

    // Get the original column information
    public Column getRawColumn() {
        Column column = new Column(this.table, this.field, this.field);
        column.setGroupByFun(this.getGroupByFun());
        column.setRowConvert(this.getRowConvert());
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

    public Function<List<Row>, Object> getGroupByFun() {
        return groupByFun;
    }

    public void setGroupByFun(Function<List<Row>, Object> groupByFun) {
        this.groupByFun = groupByFun;
    }

    public Function<Row, ?> getRowConvert() {
        return rowConvert;
    }

    public void setRowConvert(Function<Row, ?> rowConvert) {
        this.rowConvert = rowConvert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(table.getName(), column.table.getName()) && Objects.equals(field, column.field) && Objects.equals(alias, column.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, field, alias);
    }

    @Override
    public String toString() {
        return table.getName() + "." + field + "(" + alias + ")";
    }


}
