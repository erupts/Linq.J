package xyz.erupt.eql.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Dql {

    //去重
    private boolean distinct = false;

    private Collection<?> source;

    //列信息
    private List<Column> columns = new ArrayList<>();

    //关联信息
    private List<JoinSchema<?>> joinSchemas = new ArrayList<>();

    //条件过滤控制
    private List<Function<Map<Column, ?>, Boolean>> conditions = new ArrayList<>();

    //分组控制
    private List<Column> groupBys = new ArrayList<>();

    //排序控制
    private List<OrderByColumn> orderBys = new ArrayList<>();

    private Integer limit = null;

    private Integer offset = null;


    public boolean isDistinct() {
        return distinct;
    }

    public Collection<?> getSource() {
        return source;
    }

    public void setSource(Collection<?> source) {
        this.source = source;
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

    public List<Function<Map<Column, ?>, Boolean>> getConditions() {
        return conditions;
    }

    public void setConditions(List<Function<Map<Column, ?>, Boolean>> conditions) {
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
