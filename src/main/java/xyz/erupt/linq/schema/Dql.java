package xyz.erupt.linq.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Dql {

    private boolean distinct = false;

    private List<?> from;

    // columns definition
    private final List<Column> columns = new ArrayList<Column>() {
        @Override
        public boolean add(Column column) {
            super.remove(column);
            return super.add(column);
        }
    };

    // json definition
    private final List<JoinSchema<?>> joinSchemas = new ArrayList<>();

    // where definition
    private final List<WhereSchema> wheres = new ArrayList<>();

    // group by definition
    private final List<Column> groupBys = new ArrayList<>();

    // having definition
    private final List<Predicate<Row>> having = new ArrayList<>();

    // order by definition
    private final List<OrderBySchema> orderBys = new ArrayList<>();


    private Integer limit = null;

    private Integer offset = null;


    public boolean isDistinct() {
        return distinct;
    }

    public List<?> getFrom() {
        return from;
    }

    public void setFrom(List<?> from) {
        this.from = from;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<WhereSchema> getWheres() {
        return wheres;
    }

    public List<JoinSchema<?>> getJoinSchemas() {
        return joinSchemas;
    }

    public List<Column> getGroupBys() {
        return groupBys;
    }

    public List<Predicate<Row>> getHaving() {
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
