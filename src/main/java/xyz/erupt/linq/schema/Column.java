package xyz.erupt.linq.schema;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Column {

    private Class<?> table;

    private String field;

    private String alias;

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
        // Class instances are unique per classloader, so identity comparison is both
        // correct and far cheaper than comparing table.getName() strings on every call.
        return table == column.table && Objects.equals(field, column.field) && Objects.equals(alias, column.alias);
    }

    @Override
    public int hashCode() {
        // Manual computation avoids the Object[] allocation that Objects.hash() performs
        // on every call — this method is hit hard by Row lookups and HashMap operations.
        int result = table == null ? 0 : table.hashCode();
        result = 31 * result + (field == null ? 0 : field.hashCode());
        result = 31 * result + (alias == null ? 0 : alias.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return table.getName() + "." + field + " as " + alias;
    }


}
