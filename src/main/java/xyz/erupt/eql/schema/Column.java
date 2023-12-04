package xyz.erupt.eql.schema;

public class Column<T> {

    private Class<T> table;

    private String field;

    private String alias;

    public Column() {
    }

    public Column(Class<T> table, String field, String alias) {
        this.table = table;
        this.field = field;
        this.alias = alias;
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

}
