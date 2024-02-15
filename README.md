# LINQ by Java

### Best linq implementation for Java

### 操作 Java 对象和写 SQL一样的体验

### 使用方法
```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 应用场景

- 不同对象间的关联，无需转换为map再关联，代码简洁，且性能卓越
- 学习成本低，有SQL基础即可操作任意对象矩阵
- feign结果间的关联
- 多个结果对象的排序聚合与去重
- 对象转换与映射

#### query DEMO

对象定义

```javascript
 // table 
var TestSource = [{
    id: 1,
    name: "Thanos",
    date: new Date()
}, {
    id: 2,
    name: "Liz",
    date: new Date()
},
]
```

Linq

```javascript
    Linq.from(table).select().write();
```

SQL

```sql
    select *
    from table
```

结果

```json
[
  {}
]
```

#### where

#### join

```javascript
    Linq.from(source)
    .leftJoin(target, target::getId, Source::getId)
    .innerJoin(target2, target2::getId, Source::getId)
    .select(
        Columns.all(Source.class),
        Columns.of(target::getName, "name2")
    ).write();
```

```sql
    select source.*, target.name name2
    from source
             left join target on source.id = target.id
             inner join target2 on source.id = target2.id
```

#### group by

```javascript
     Linq.from(source).groupBy(Columns.of(TestSource::getName))
    .select(
        Columns.of(TestSource::getName, "name"),
        Columns.min(TestSource::getDate, "min"),
        Columns.avg(TestSource::getId, "avg"),
        Columns.count("count"),
        Columns.count(TestSource::getName, "countName"),
        Columns.countDistinct(TestSource::getName, "countDistinct")
    ).orderBy(TestSource::getName).writeMap();
```

```sql
    select name,
           min(date)            min,
           avg(id)              avg,
           count(*)             count,
           count(name)          countName,
           count(distinct name) countDistinct
    from source
    group by name
```

#### order by

#### limit

### 后续迭代计划

- group by 支持自定义分组 key 格式化
- 支持 having
- 支持多个查询结果集进行组合: UNION ALL、UNION、INTERSECT、EXCEPT、
- 支持窗口函数
- 支持自定义条件 join