package xyz.erupt.linq.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Dql {

    private boolean distinct = false;

    private Collection<?> from;

    // columns definition
    private List<Column> columns = new ArrayList<>();

    // json definition
    private List<JoinSchema<?>> joinSchemas = new ArrayList<>();

    // definition definition
    private List<Function<Row, Boolean>> conditions = new ArrayList<>();

    // group by definition
    private List<Column> groupBys = new ArrayList<>();

    // order by definition
    private List<OrderByColumn> orderBys = new ArrayList<>();

    private Integer limit = null;

    private Integer offset = null;


    public boolean isDistinct() {
        return distinct;
    }

    public Collection<?> getFrom() {
        return from;
    }

    public void setFrom(Collection<?> from) {
        this.from = from;
    }


    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Function<Row, Boolean>> getConditions() {
        return conditions;
    }

    public void setConditions(List<Function<Row, Boolean>> conditions) {
        this.conditions = conditions;
    }

    public List<JoinSchema<?>> getJoinSchemas() {
        return joinSchemas;
    }

    public void setJoinSchemas(List<JoinSchema<?>> joinSchemas) {
        this.joinSchemas = joinSchemas;
    }

    public List<Column> getGroupBys() {
        return groupBys;
    }

    public void setGroupBys(List<Column> groupBys) {
        this.groupBys = groupBys;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<OrderByColumn> getOrderBys() {
        return orderBys;
    }

    public void setOrderBys(List<OrderByColumn> orderBys) {
        this.orderBys = orderBys;
    }
}
