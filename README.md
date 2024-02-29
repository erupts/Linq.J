# LINQ in Java
> Linq是面向对象的sql，linq实际上是对内存里的数据的查询

可以通过最少的代码对数据源进行关联、筛选、排序和分组等操作

允许编写Java代码以查询数据库相同的方式操作内存数据，如
- List 集合中的数据
- SQL 结果的数据
- Array 数组中的数据
- CSV 数据集
- XML 文档
- JSON 文档
- Stream 流

### 应用场景
- 分布式开发时Feign / Dubbo的结果关联
- 多个结果对象的排序聚合与内存分页
- 语义化对象转换与映射
- 联邦查询

### 操作语法
- From
- Select
- Distinct
- Join
- Where
- Group By
- Order By
- Limit
- Offset
- ...

### 使用方法
包内零外部依赖，包体仅仅50kb
```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 应用场景

- 不同对象间的关联，无需转换为map再关联，代码简洁，且性能卓越
- 不同数据集结果间的关联
- 联邦查询
- 多个结果对象的排序聚合与去重
- 对象转换与映射

### 优点

- 学习成本低，有SQL基础即可操作任意对象
- 代码简洁，无需for循环与分支操作数据
- 执行效率高10W级数据毫秒级处理
- 轻量级，零外部依赖

### DEMO

对象定义

```
// table 
List<TestSource> testSource = [{
    id: 1,
    name: "Thanos",
    date: new Date()
}, {
    id: 2,
    name: "Liz",
    date: new Date()
}
]
```

### select

```javascript
List<String> strings = Linq.from("C", "A", "B", "B").gt(Th::is, "A").orderByDesc(Th::is).write(String.class);
// [C, B, B]


List<Integer> integers = Linq.from(1, 2, 3, 7, 6, 5).orderBy(Th::is).write(Integer.class);
// [1, 2, 3, 5, 6, 7]

List<String> names = Linq.from(testSource)
    .select(Columns.of(TestSource::getName))
    .write(String.class);
// ["Thanos", "Liz"]
```

### join

```javascript
    Linq.from(source)
    .leftJoin(target, Target::getId, Source::getId)
    .innerJoin(target2, Target2::getId, Source::getId)
    .select(
        Columns.all(Source.class),
        Columns.of(Target::getName, "name2")
    ).write();
```

Corresponding SQL

```sql
    select source.*, target.name name2
    from source
             left join target on source.id = target.id
             inner join target2 on source.id = target2.id
```

### group by

```javascript

Linq.from(source).groupBy(Columns.of(TestSource::getName))
    .select(
        Columns.of(TestSource::getName, "name"), // or: Columns.of(TestSource::getName, TestSource::getName)
        Columns.min(TestSource::getDate, "min"),
        Columns.avg(TestSource::getId, "avg"),
        Columns.count("count"),
        Columns.count(TestSource::getName, "countName"),
        Columns.countDistinct(TestSource::getName, "countDistinct")
    ).orderBy(TestSource::getName).writeMap();
```

result

```json
 [
  {
    "name": "Thanos",
    "min": 10,
    "avg": 1.0,
    "count": 2,
    "countName": 1,
    "countDistinct": 1
  },
  {
    "name": "Liz",
    "min": 10,
    "avg": 3.0,
    "count": 4,
    "countName": 2,
    "countDistinct": 1
  }
]
```

Corresponding SQL

```sql
    select name,
           min(date) min,
           avg(id)              avg,
           count(*)             count,
           count(name)          countName,
           count(distinct name) countDistinct
    from source
    group by name
```

### 后续迭代计划
> 大家的支持才是持续迭代的动力！
- 支持多个查询结果集进行组合: UNION ALL、UNION、INTERSECT、EXCEPT、UNION BY NAME
- 支持窗口函数
- 支持 Nested loop join
- 支持 having
- group by 支持自定义分组 key 格式化