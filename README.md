# LINQ by Java

### Best linq implementation

### 操作 Java 对象和写 SQL一样的体验

#### simple query

```javascript
    Linq.from(table).select().write();
```
```sql
    select * from table
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
