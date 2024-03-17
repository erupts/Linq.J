package xyz.erupt.linq.schema;

import java.util.*;
import java.util.function.Function;

public class Dql {

    private boolean distinct = false;

    private Collection<?> from;

    // columns definition
    private List<Column> columns = new ArrayList<>();

    // json definition
    private final List<JoinSchema<?>> joinSchemas = new ArrayList<>();

    // where definition
    private final List<Function<Row, Boolean>> wheres = new ArrayList<>();

    // group by definition
    private final List<Column> groupBys = new ArrayList<>();

    // having definition
    private final List<Function<Row, Boolean>> having = new ArrayList<>();


    // order by definition
    private final List<OrderBySchema> orderBys = new ArrayList<>();

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

    public List<Function<Row, Boolean>> getWheres() {
        return wheres;
    }

    public List<JoinSchema<?>> getJoinSchemas() {
        return joinSchemas;
    }

    public List<Column> getGroupBys() {
        return groupBys;
    }

    public List<Function<Row, Boolean>> getHaving() {
        return having;
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

    public List<OrderBySchema> getOrderBys() {
        return orderBys;
    }

}
