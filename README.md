# Linq.J 基于内存的对象查询语言
`Java 中使用类似 C# 的 Linq 能力 ` [C# Linq](https://learn.microsoft.com/zh-cn/dotnet/csharp/linq/)

<p>
    <a href="https://www.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/Linq.J-brightgreen" alt="Erupt Framework"></a>
    <a href="https://mvnrepository.com/search?q=linq.j"><img src="https://img.shields.io/maven-central/v/xyz.erupt/linq.j" alt="maven-central"></a>
    <a href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="jdk 8+"></a>
    <a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue" alt="license Apache 2.0"></a>
    <a href='https://gitee.com/erupt/linq/stargazers'><img src='https://gitee.com/erupt/linq/badge/star.svg?theme=gray' alt='GitEE star' ></img></a>
    <a href="https://github.com/erupts/linq.j"><img src="https://img.shields.io/github/stars/erupts/linq.j?style=social" alt="GitHub stars"></a>
</p>



### Linq 是面向对象的 sql，linq实际上是对内存中数据的查询，使开发人员能够更容易地编写查询。这些查询表达式看起来很像SQL

> 可以通过最少的代码对数据源进行关联、筛选、排序和分组等操作。这些操作可以在单个查询中组合起来，以获得更复杂的结果，无需for循环与if分支操作数据，内置查询引擎性能卓越

#### 允许编写Java代码以查询数据库相同的方式操作内存数据，例如
- List 集合、Array 数组中的数据
- CSV、XML、JSON 文档数据集
- Stream、File 流
- Redis、SQL、MongoDB数据库结果集

#### 应用场景
- 分布式开发时 Feign / Dubbo / gRPC / WebService 等 RPC 结果的关联/筛选/聚合/分页
- 语义化对象转换与映射，团队协作代码更清晰敏捷
- 异构系统数据的内存计算
- 使用代码组织 SQL 结果数据
- 跨数据源的联邦访问

#### 操作语法
`From` `Select` `Distinct`、`Join`、`Where`、`Group By`、`Order By`、`Limit`、`Offset`、`...`

#### 使用提示
⚠️ 注意：操作的对象字段必须存在 get 方法便于 lambda 查找，建议配合 **Lombok** 的 @Getter 注解快速创建字段的 get 访问

#### 使用方法
```xml
<!--包内零外部依赖，体积仅仅50kb-->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq.j</artifactId>
    <version>0.0.4</version>
</dependency>
```

#### Example 1
```javascript
var strings = Linq.from("C", "A", "B", "B").gt(Th::is, "A").orderByDesc(Th::is).write(String.class);
// [C, B, B]

var integers = Linq.from(1, 2, 3, 7, 6, 5).orderBy(Th::is).write(Integer.class);
// [1, 2, 3, 5, 6, 7]

var name = Linq.from(data)
    // left join
    .innerJoin(target, Target::getId, Data::getId)
    // where like
    .like(Data::getName, "a")
    // select name
    .select(Data::getName)
    // distinct
    .distinct()
    // order by 
    .orderBy(Data::getName)
    .write(String.class);

```

#### Example 2
```java
public class ObjectQuery{

    private final List<TestSource> source = http.get("https://gw.alipayobjects.com/os/antfincdn/v6MvZBUBsQ/column-data.json");

    private final List<TestSourceExt> target = mongodb.query("db.target.find()");
    
    /**
     * select demo
     */
    public void select(){
        // select *
        Linq.from(source).select(Columns.all(TestSource.class));
        // select a, b, c
        Linq.from(source)
                .select(TestSource::getName, TestSource::getDate, TestSource::getTags)
                .select(Columns.of(TestSource::getTags, "tag2")) // alias
                .select(Columns.ofx(TestSource::getId, id -> id + "xxx")); // value convert
        // select count(*), sum(id), max(id) 
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
            .select(
                Columns.of(TestSource::getName, "name"),
                Columns.min(TestSource::getDate, "min"),
                Columns.avg(TestSource::getId, "avg"),
                Columns.count("count"),
                Columns.count(TestSource::getName, "countName"),
                Columns.countDistinct(TestSource::getName, "countDistinct")
            )
            .having(row -> Integer.parseInt(row.get("avg").toString()) > 2)
            .orderBy(TestSource::getAge);
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
    
}

```

#### 后续迭代计划

- [ ] 支持多个查询结果集进行组合: UNION ALL、UNION、INTERSECT、EXCEPT、UNION BY NAME
- [ ] 支持窗口函数
- [ ] 支持 Nested loop join
- [x] 支持 having
- [x] 支持分组列格式化 group by date(created_at)
