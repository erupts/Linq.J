package xyz.erupt.linq.schema;

import java.util.function.Function;

public class WhereSchema {

    private Function<Row, Boolean> condition;

    private Column relationColumn;

    public WhereSchema(Function<Row, Boolean> condition, Column relationColumn) {
        this.condition = condition;
        this.relationColumn = relationColumn;
    }

    public Function<Row, Boolean> getCondition() {
        return condition;
    }

    public void setCondition(Function<Row, Boolean> condition) {
        this.condition = condition;
    }

    public Column getRelationColumn() {
        return relationColumn;
    }

    public void setRelationColumn(Column relationColumn) {
        this.relationColumn = relationColumn;
    }
}
