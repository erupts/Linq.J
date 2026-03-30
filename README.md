<div align="center">

# Linq.J

### A Lightweight, Zero-Dependency Object Query Language for Java

**Query in-memory data the way you query a database — with SQL-like fluent API inspired by [C# LINQ](https://learn.microsoft.com/en-us/dotnet/csharp/linq/)**

<p>
    <a href="https://www.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/Linq.J-brightgreen" alt="Erupt Framework" /></a>
    <a href="https://mvnrepository.com/search?q=linq.j"><img src="https://img.shields.io/maven-central/v/xyz.erupt/linq.j" alt="maven-central" /></a>
    <a href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="jdk 8+" /></a>
    <a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue" alt="license MIT"></a>
    <a href="https://github.com/erupts/linq.j"><img src="https://img.shields.io/github/stars/erupts/linq.j?style=social" alt="GitHub stars" /></a>
    <a href='https://gitee.com/erupt/linq/stargazers'><img src='https://gitee.com/erupt/linq/badge/star.svg?theme=gray' alt='GitEE star' /></a>
    <a href='https://gitcode.com/erupts/Linq.J'><img src='https://gitcode.com/erupts/linq.j/star/badge.svg' alt='GitCode star' /></a>
</p>

[中文](./README-zh.md) / English

</div>

---

## Highlights

- **Zero Runtime Dependencies** — pure JDK, no third-party libraries
- **~50 KB** — minimal footprint, won't bloat your project
- **JDK 8+** — works on Java 8 and above
- **Type-Safe** — lambda method references for column operations, compile-time checked
- **SQL-Like** — `SELECT`, `JOIN`, `WHERE`, `GROUP BY`, `HAVING`, `ORDER BY`, `LIMIT`, `OFFSET`, `DISTINCT`
- **Pluggable Engine** — swap or extend the default `EruptEngine` with your own execution strategy

## Why Linq.J?

Java developers often need to join, filter, sort, and aggregate data from **in-memory collections** — results from RPCs, heterogeneous data sources, or post-SQL processing. Without Linq.J, this means verbose `for` loops, `if` branches, and scattered logic. Linq.J replaces all of that with a **single fluent chain** that reads like SQL.

```java
var result = Linq.from(employees)
    .innerJoin(departments, Dept::getId, Emp::getDeptId)
    .where(Emp::getSalary, salary -> salary > 5000)
    .groupBy(Dept::getName)
    .select(
        Columns.of(Dept::getName, "department"),
        Columns.avg(Emp::getSalary, "avgSalary"),
        Columns.count("headcount")
    )
    .orderByDesc(Emp::getSalary)
    .write(DeptStats.class);
```

## Getting Started

### 1. Add the Dependency

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq.j</artifactId>
    <version>LATEST</version>
</dependency>
```

### 2. Ensure Fields Have Getters

Linq.J resolves field names from **lambda method references** via `SerializedLambda`. Your data classes must have getter methods. Using [Lombok](https://projectlombok.org/) `@Getter` is recommended:

```java
@Getter
public class User {
    private Long id;
    private String name;
    private Integer age;
}
```

### 3. Start Querying

```java
List<User> adults = Linq.from(users)
    .where(User::getAge, age -> age >= 18)
    .select(User::getName, User::getAge)
    .orderBy(User::getAge)
    .write(User.class);
```

## Supported Data Sources

Linq.J operates on any **in-memory collection**. Feed it data from anywhere:

| Source | Example |
|--------|---------|
| List / Array | `Linq.from(list)` or `Linq.from("A", "B", "C")` |
| SQL Result | Load via JDBC / MyBatis / JPA, then query with Linq |
| CSV / XML / JSON | Parse into objects, then query with Linq |
| Redis / MongoDB | Fetch results, then query with Linq |
| Stream / File | Collect into a list, then query with Linq |
| RPC Response | Feign / Dubbo / gRPC results, then query with Linq |

## API Reference

### Select

```java
// Select all fields
Linq.from(source).select(User.class);

// Select specific fields
Linq.from(source).select(User::getName, User::getDate, User::getTags);

// Select with alias
Linq.from(source).select(User::getTags, "tagAlias");

// Select with value transformation
Linq.from(source).select(Columns.ofx(User::getId, id -> id + "-suffix"));

// Aggregate functions
Linq.from(source).select(
    Columns.count("count"),
    Columns.sum(User::getId, "sum"),
    Columns.max(User::getId, "max"),
    Columns.min(User::getId, "min"),
    Columns.avg(User::getId, "avg"),
    Columns.countDistinct(User::getName, "uniqueNames")
);
```

### Join

Four standard join types are supported, powered by a Hash Join engine for performance:

```java
// Left Join
Linq.from(source).leftJoin(target, Target::getId, Source::getId);

// Right Join
Linq.from(source).rightJoin(target, Target::getId, Source::getId);

// Inner Join
Linq.from(source).innerJoin(target, Target::getId, Source::getId);

// Full Join
Linq.from(source).fullJoin(target, Target::getId, Source::getId);

// Join + multi-table select
Linq.from(source)
    .leftJoin(target, Target::getId, Source::getId)
    .select(Source.class)
    .select(Target::getName)
    .write(Result.class);
```

### Where

```java
// Equals
Linq.from(source).eq(User::getName, "Thanos");

// Between (inclusive)
Linq.from(source).between(User::getId, 1, 100);

// In
Linq.from(source).in(User::getId, 1, 2, 3);

// Like (contains)
Linq.from(source).like(User::getName, "admin");

// Is Null
Linq.from(source).isNull(User::getId);

// Greater than / Less than
Linq.from(source).gt(User::getAge, 18);

// Custom single-field condition
Linq.from(source).where(User::getId, id -> id >= 5);

// Custom multi-field condition
Linq.from(source).where(row -> {
    String name = row.get(User::getName);
    Integer age = (Integer) row.get(User::getAge);
    return "admin".equals(name) || age > 18;
});
```

### Group By & Having

```java
Linq.from(orders)
    .groupBy(Order::getCategory)
    .select(
        Columns.of(Order::getCategory, "name"),
        Columns.min(Order::getDate, "earliest"),
        Columns.avg(Order::getPrice, "avgPrice"),
        Columns.count("total"),
        Columns.countDistinct(Order::getBuyer, "uniqueBuyers")
    )
    .having(row -> Integer.parseInt(row.get("total").toString()) > 10)
    .orderBy(Order::getPrice)
    .write(CategoryStats.class);
```

### Order By, Limit & Offset

```java
Linq.from(source)
    .orderBy(User::getName)          // ascending
    .orderByDesc(User::getAge)       // descending
    .offset(10)                      // skip first 10
    .limit(20)                       // take 20 records
    .write(User.class);
```

### Result Output

```java
// Write to List<T>
List<User> list = Linq.from(source).write(User.class);

// Write to single object
User one = Linq.from(source).limit(1).writeOne(User.class);

// Write to List<Map<String, Object>>
List<Map<String, Object>> maps = Linq.from(source).writeMap();

// Write to single Map<String, Object>
Map<String, Object> map = Linq.from(source).writeMapOne();
```

## Architecture

Linq.J follows a clean **layered architecture** with separation between query building and execution:

```
┌─────────────────────────────────────────────────────┐
│                  Fluent API Layer                    │
│   Select · Join · Where · GroupBy · OrderBy · Write  │
├─────────────────────────────────────────────────────┤
│                  Query Model Layer                   │
│        Linq (Facade) · Dql (State) · Column · Row    │
├─────────────────────────────────────────────────────┤
│                 Execution Engine Layer                │
│       Engine (Abstract) → EruptEngine (Default)      │
├─────────────────────────────────────────────────────┤
│                Lambda Resolution Layer               │
│     SFunction · LambdaSee · SerializedLambda         │
└─────────────────────────────────────────────────────┘
```

**Pluggable Engine**: Replace the default `EruptEngine` with a custom implementation:

```java
Linq.setEngine(new MyCustomEngine());
```

## Use Cases

| Scenario | Description |
|----------|-------------|
| **RPC Result Association** | Join results from Feign / Dubbo / gRPC calls in memory instead of multiple DB roundtrips |
| **Heterogeneous Data** | Unify and query data from Redis, MongoDB, MySQL in one place |
| **Post-SQL Processing** | Further filter, sort, and aggregate database query results in code |
| **In-Memory Pagination** | Merge multiple result sets, then sort, aggregate, and paginate |
| **Object Mapping** | Semantic, readable object transformation and projection |
| **Cross-Source Federation** | Federated queries across different data sources at the application layer |

## Before & After

<table>
<tr>
<td width="50%">

**Traditional Java**

```java
Map<String, List<Order>> grouped = new HashMap<>();
for (Order o : orders) {
    if (o.getAmount() > 100) {
        grouped.computeIfAbsent(
            o.getCategory(),
            k -> new ArrayList<>()
        ).add(o);
    }
}
Map<String, Double> avgMap = new HashMap<>();
for (var entry : grouped.entrySet()) {
    double sum = 0;
    for (var o : entry.getValue()) {
        sum += o.getAmount();
    }
    avgMap.put(entry.getKey(),
        sum / entry.getValue().size());
}
// Still need sorting... more code...
```

</td>
<td width="50%">

**With Linq.J**

```java
var result = Linq.from(orders)
    .where(Order::getAmount, a -> a > 100)
    .groupBy(Order::getCategory)
    .select(
        Columns.of(Order::getCategory, "cat"),
        Columns.avg(Order::getAmount, "avg"),
        Columns.count("cnt")
    )
    .orderByDesc(Order::getAmount)
    .write(Stats.class);
```

</td>
</tr>
</table>

## Roadmap

- [x] HAVING clause support
- [x] Group column formatting (`group by date(created_at)`)
- [ ] Set operations: UNION ALL, UNION, INTERSECT, EXCEPT, UNION BY NAME
- [ ] Window functions (ROW_NUMBER, RANK, DENSE_RANK, ...)
- [ ] Nested Loop Join strategy

## Contributing

Contributions are welcome! Feel free to open issues and pull requests.

## License

[MIT](./LICENSE)
