# Linq.J 基于 JVM → Lambda 特性的的联邦分析库
> Linq 是面向对象的 sql，linq实际上是对内存中数据的查询，使开发人员能够更容易地编写查询。这些查询表达式看起来很像SQL

> 可以通过最少的代码对数据源进行关联、筛选、排序和分组等操作。这些操作可以在单个查询中组合起来，以获得更复杂的结果

> 学习成本低，有 SQL 基础即可操作任意对象


允许编写Java代码以查询数据库相同的方式操作内存数据，例如
- List 集合、Array 数组中的数据
- SQL 结果的数据
- CSV、XML、JSON 文档数据集
- Stream、File 流

### 应用场景
- 分布式开发时 Feign / Dubbo 等 RPC 的结果关联
- 使用代码组织 SQL 结果数据
- 多个结果对象的排序聚合与内存分页
- 语义化对象转换与映射
- 跨数据源的联邦访问

### 操作语法
> From、Select、Distinct、Join、Where、Group By、Order By、Limit、Offset、...

### 使用方法
包内零外部依赖，体积仅仅50kb
```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 优点

- 学习成本低，有SQL基础即可操作任意对象
- 代码简洁，无需for循环与分支操作数据
- 执行效率高10W级数据毫秒级处理
- 轻量级，零外部依赖

### DEMO
```javascript
var strings = Linq.from("C", "A", "B", "B").gt(Th::is, "A").orderByDesc(Th::is).write(String.class);
// [C, B, B]
var integers = Linq.from(1, 2, 3, 7, 6, 5).orderBy(Th::is).write(Integer.class);
// [1, 2, 3, 5, 6, 7]
```


```java
public class ObjectQuery{

    private final List<TestSource> source = mysql.query("select * form source");

    private final List<TestSourceExt> target = mongo.query("db.target.find()");
    
    /**
     * select demo
     */
    public void select(){
        Linq.from(source).select(Columns.all(TestSource.class));
        
        Linq.from(source)
                .select(TestSource::getId, TestSource::getName, TestSource::getDate)
                .select(Columns.of(TestSource::getTags))
                .select(Columns.of(TestSource::getTags, "tag2"));
        
        Linq.from(source)
                .select(Columns.count("count"))
                .select(Columns.sum(TestSource::getId, "sum"))
                .select(Columns.max(TestSource::getId, "max"));
    }

    /**
     * join demo
     */
    public void join(){
        // left join
        Linq.from(source).leftJoin(target, TestSourceExt::getId, TestSource::getId).select(
            Columns.all(TestSource.class),
            Columns.of(TestSourceExt::getName),
            Columns.of(TestSourceExt2::getValue)
        );
        // right join
        Linq.from(source).rightJoin(target, TestSourceExt::getId, TestSource::getId);
        // inner join
        Linq.from(source).innerJoin(target, TestSourceExt::getId, TestSource::getId);
        // full join
        Linq.from(source).fullJoin(target, TestSourceExt::getId, TestSource::getId);
    }

    /**
     * where demo
     */
    public void where() {
        // =
        Linq.from(source).eq(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        // >=:lval and <=:rval
        Linq.from(source).between(TestSource::getId, 1, 3);
        // in (x,x,x)
        Linq.from(source).in(TestSource::getId, 1, 2, 3);
        // like '%x%'
        Linq.from(source).like(TestSource::getName, "a");
        // is null
        Linq.from(source).isNull(TestSource::getId);
        
        // customer single field where
        Linq.from(source).where(TestSource::getId, id -> id >= 5);
        
        // customer condition or multi field
        Linq.from(source).condition(data -> {
            String name = data.get(TestSource::getName);
            Integer age = (Integer)data.get(TestSource::getAge);
            // name = 'xxx' or age > 10
            return "xxx".equals(name) || age > 10;
        });
    }

    /**
     * group by demo
     */
    public void groupBy(){
        Linq.from(source)
            .groupBy(TestSource::getName)
            .orderBy(TestSource::getAge)
            .select(
                Columns.of(TestSource::getName, "name"),
                Columns.min(TestSource::getDate, "min"),
                Columns.avg(TestSource::getId, "avg"),
                Columns.count("count"),
                Columns.count(TestSource::getName, "countName"),
                Columns.countDistinct(TestSource::getName, "countDistinct")
            );
    }

    /**
     * result write demo
     */
    public void write(){
        // write List<Object>
        List<TestSource> list = Linq.from(source).orderByAsc(TestSource::getDate).write(TestSource.class);
        // write Object
        TestSource obj = Linq.from(source).limit(3).writeOne(TestSource.class);
        // write List<Map>
        List<Map<String, Object>> map = Linq.from(source).writeMap();
        // write Map
        Map<String, Object> mapOne = Linq.from(source).writeMapOne();
    }

    public void other(){
        String name = Linq.from(source)
                .select(TestSource::getName)
                .distinct()
                .limit(2)
                .offset(5)
                .orderBy(TestSource::getName)
                .orderByDesc(TestSource::getAge)
                .writeOne(String.class);
    }
    
}

```

### 后续迭代计划
> 大家的支持才是持续迭代的动力！

- 支持多个查询结果集进行组合: UNION ALL、UNION、INTERSECT、EXCEPT、UNION BY NAME
- 支持窗口函数
- 支持 Nested loop join
- 支持 having
- group by 支持自定义分组 key 格式化
