package xyz.erupt.eql.schema;

import xyz.erupt.eql.grammar.OrderBy;

public class OrderByColumn {

    private Column<?> column;

    private OrderBy.Direction direction;

    public OrderByColumn() {
    }

    public OrderByColumn(Column<?> column, OrderBy.Direction direction) {
        this.column = column;
        this.direction = direction;
    }

    public Column<?> getColumn() {
        return column;
    }

    public void setColumn(Column<?> column) {
        this.column = column;
    }

    public OrderBy.Direction getDirection() {
        return direction;
    }

    public void setDirection(OrderBy.Direction direction) {
        this.direction = direction;
    }
}
