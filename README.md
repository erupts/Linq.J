# Linq.J A Memory-based Object Query language
[中文](./README-zh.md) / English

`Java uses Linq capabilities similar to C#` [C# Linq](https://learn.microsoft.com/zh-cn/dotnet/csharp/linq/)

<p>
    <a href="https://www.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/Linq.J-brightgreen" alt="Erupt Framework" /></a>
    <a href="https://mvnrepository.com/search?q=linq.j"><img src="https://img.shields.io/maven-central/v/xyz.erupt/linq.j" alt="maven-central" /></a>
    <a href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="jdk 8+" /></a>
    <a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue" alt="license Apache 2.0"></a>
    <a href='https://gitcode.com/erupts/Linq.J'><img src='https://gitcode.com/erupts/linq.j/star/badge.svg' alt='GitCode star' /></a>
    <a href='https://gitee.com/erupt/linq/stargazers'><img src='https://gitee.com/erupt/linq/badge/star.svg?theme=gray' alt='GitEE star' /></a>
    <a href="https://github.com/erupts/linq.j"><img src="https://img.shields.io/github/stars/erupts/linq.j?style=social" alt="GitHub stars" /></a>
</p>

### Linq is Object oriented sql, linq is actually a query on the data in memory, enabling developers to write queries more easily. These query expressions look a lot like SQL

> You can join, filter, sort, and group data sources with minimal code. These operations can be combined in a single query to obtain more complex results

#### allows you to write Java code that manipulates in-memory data in the same way that you query a database, for example
- List, Array
- SQL result data
- CSV, XML, JSON document datasets
- Stream, File stream

#### Application Scenarios
- Result association for RPCS such as Feign/Dubbo during distributed development
- In-memory computation of heterogeneous system data
- Use code to organize SQL result data
- Sorted aggregation of multiple result objects with in-memory paging
- Semantic object transformation and mapping
- Clean code, no need for loops and branches to manipulate data
- Federated access across data sources

#### Operation syntax
`From` `Select` `Distinct`、`Join`、`Where`、`Group By`、`Order By`、`Limit`、`Offset`、`...`

#### Tips
⚠️ Note: The object field must have a get method to facilitate lambda lookup. It is recommended to use the **Lombok** @Getter annotation to quickly create get access to the field

#### How to Use
It has zero external dependencies and is only 50kb in size
```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq.j</artifactId>
    <version>0.0.6</version>
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
        Linq.from(source).select(TestSource.class);
        // select a, b, c
        Linq.from(source)
                .select(TestSource::getName, TestSource::getDate, TestSource::getTags)
                .select(TestSource::getTags, "tag2") // alias
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
        Linq.from(source).leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(TestSource.class)
                .select(TestSourceExt::getName)
                .select(TestSourceExt2::getValue);
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
        Linq.from(source).where(data -> {
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

#### Next iteration plan

- [ ] Supports combining multiple query result sets: UNION ALL, UNION, INTERSECT, EXCEPT, UNION BY NAME
- [ ] Supports window functions
- [ ] Support Nested loop join
- [x] supports having
- [x] Support group column format group by date(created_at)