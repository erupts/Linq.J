package xyz.erupt.linq.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
