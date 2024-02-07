package xyz.erupt.linq.schema;

import xyz.erupt.linq.consts.OrderByDirection;

public class OrderBySchema {

    private Column column;

    private OrderByDirection direction;

    public OrderBySchema() {
    }

    public OrderBySchema(Column column, OrderByDirection direction) {
        this.column = column;
        this.direction = direction;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public OrderByDirection getDirection() {
        return direction;
    }

    public void setDirection(OrderByDirection direction) {
        this.direction = direction;
    }

}
