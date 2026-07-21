package xyz.erupt.linq.schema;

import java.util.function.Predicate;

public class WhereSchema {

    private Predicate<Row> condition;

    private Column relationColumn;

    // Pushdown form of a typed where: applied to the raw column value read straight off the
    // source object, letting the engine filter BEFORE row materialization. Null for row-level
    // conditions, which can only run after materialization.
    private Predicate<Object> valueCondition;

    public WhereSchema(Predicate<Row> condition, Column relationColumn) {
        this.condition = condition;
        this.relationColumn = relationColumn;
    }

    public Predicate<Row> getCondition() {
        return condition;
    }

    public void setCondition(Predicate<Row> condition) {
        this.condition = condition;
    }

    public Column getRelationColumn() {
        return relationColumn;
    }

    public void setRelationColumn(Column relationColumn) {
        this.relationColumn = relationColumn;
    }

    public Predicate<Object> getValueCondition() {
        return valueCondition;
    }

    public void setValueCondition(Predicate<Object> valueCondition) {
        this.valueCondition = valueCondition;
    }
}
