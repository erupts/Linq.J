package xyz.erupt.linq.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Dql {

    //去重
    private boolean distinct = false;

    //列信息
    private List<Column> columns = new ArrayList<>();

    //关联信息
    private List<JoinSchema<?, ?>> joinSchemas = new ArrayList<>();

    //条件信息
    private List<Function<?, ?>> conditions = new ArrayList<>();

    //分组信息
    private List<Column> groupBys = new ArrayList<>();

    public boolean isDistinct() {
        return distinct;
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

    public List<Function<?, ?>> getConditions() {
        return conditions;
    }

    public void setConditions(List<Function<?, ?>> conditions) {
        this.conditions = conditions;
    }

    public List<JoinSchema<?, ?>> getJoinSchemas() {
        return joinSchemas;
    }

    public void setJoinSchemas(List<JoinSchema<?, ?>> joinSchemas) {
        this.joinSchemas = joinSchemas;
    }

    public List<Column> getGroupBys() {
        return groupBys;
    }

    public void setGroupBys(List<Column> groupBys) {
        this.groupBys = groupBys;
    }
}
