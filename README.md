<div align="center">

<img src="docs/logo-full.svg" alt="Linq.J" width="400">

### No more `for` loops — query in-memory data with an SQL mindset

**A lightweight, zero-dependency object query language for Java, with a fluent SQL-like API inspired by [C# LINQ](https://learn.microsoft.com/en-us/dotnet/csharp/linq/).**

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

Linq.J makes Java collection queries as intuitive as writing SQL. `JOIN`, `WHERE`, `GROUP BY`, `ORDER BY` — all in one fluent chain. Code is documentation.

```java
// SELECT dept.name, AVG(salary), COUNT(*)
// FROM employees INNER JOIN departments ON dept.id = emp.deptId
// WHERE salary > 5000
// GROUP BY dept.name ORDER BY salary DESC
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
    .toList(DeptStats.class);
```

<div align="center">

| Dependencies | Package Size | Min Version | License |
|:---:|:---:|:---:|:---:|
| **0** | **~50 KB** | **JDK 8+** | **MIT** |

</div>

## ✨ Why Linq.J?

Lightweight, powerful, and elegant — tackle the most complex queries with minimal code.

| | |
|---|---|
| **⚡ Zero Deps · Ultra Lightweight** | No third-party runtime dependencies, only ~50 KB. Drop it in and go — zero project overhead. |
| **🔗 SQL-Like Fluent API** | `Select`, `Join`, `Where`, `Group By`, `Order By`, `Limit`… operate on in-memory data using familiar SQL thinking. |
| **🛡️ Type-Safe** | Column operations via lambda method references — field names are checked at compile time, IDE auto-completes, no magic strings. |
| **🗃️ Multi-Source** | List, Array, SQL result sets, CSV/JSON/XML, Stream… any in-memory collection is queryable. |
| **🔌 Pluggable Engine** | Abstract `Engine` interface; the default `EruptEngine` ships Hash Join and other efficient strategies, fully customizable. |
| **📊 Rich Aggregations** | `COUNT`, `SUM`, `AVG`, `MAX`, `MIN`, `COUNT DISTINCT`… full aggregation capability, works seamlessly with `Group By`. |

## 🔤 SQL, one-to-one

Linq.J maps each SQL clause to a chainable method, evaluated in this order:

```
FROM → JOIN → WHERE → GROUP BY → SELECT → HAVING → ORDER BY → LIMIT → toList
```

| Clause | Methods |
|--------|---------|
| **JOIN** | `leftJoin()` · `rightJoin()` · `innerJoin()` · `fullJoin()` |
| **WHERE** | `eq()` · `ne()` · `like()` · `between()` · `in()` · `notIn()` · `isNull()` · `gt()` / `lt()` / `gte()` / `lte()` · `where(λ)` |
| **SELECT** | `select(Class)` · `select(Field...)` · `selectAs()` · `count()` · `sum()` · `avg()` · `max()` / `min()` · `countDistinct()` |
| **TERMINAL** | `toList(Class)` · `one(Class)` · `first(Class)` · `toMaps()` · `toMap()` · `count()` · `exists()` |

## 🚀 Getting Started

### 1. Add the Dependency

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>linq.j</artifactId>
    <version>${LATEST}</version>
</dependency>
```

### 2. Ensure Fields Have Getters

Linq.J resolves field names from **lambda method references** via `SerializedLambda`. Your data classes must expose getter methods — [Lombok](https://projectlombok.org/) `@Getter` is recommended:

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
    .toList(User.class);
```

## 📖 Examples

### Basic Query

Works on primitives and custom objects alike. `It::self` refers to the element itself for simple-typed sources.

```java
// String filter + sort
var strings = Linq.from("C", "A", "B", "B")
    .gt(It::self, "A")
    .orderByDesc(It::self)
    .toList(String.class);          // [C, B, B]

// Number sort
var integers = Linq.from(1, 2, 3, 7, 6, 5)
    .orderBy(It::self)
    .toList(Integer.class);         // [1, 2, 3, 5, 6, 7]
```

### Multi-Table Join

Four standard join types, powered by a Hash Join engine, syntax mirrors SQL exactly.

```java
// Left Join + multi-table select
Linq.from(source)
    .leftJoin(target, Target::getId, Source::getId)
    .select(Source.class)
    .select(Target::getName)
    .toList(Result.class);

// Inner Join + Where + Distinct
var names = Linq.from(data)
    .innerJoin(target, Target::getId, Data::getId)
    .like(Data::getName, "a")
    .select(Data::getName)
    .distinct()
    .orderBy(Data::getName)
    .toList(String.class);
```

### Filtering

From simple equality to custom multi-field predicates.

```java
Linq.from(source).eq(User::getName, "Thanos");           // exact match
Linq.from(source).between(User::getId, 1, 100);          // range (inclusive)
Linq.from(source).in(User::getId, 1, 2, 3);              // in list
Linq.from(source).like(User::getName, "admin");          // contains
Linq.from(source).where(User::getId, id -> id >= 5);     // single-field predicate

// multi-field predicate over the whole row
Linq.from(source).where(row -> {
    String name = row.get(User::getName);
    Integer age = (Integer) row.get(User::getAge);
    return "admin".equals(name) || age > 18;
});
```

### Grouping & Aggregation

`Group By` + aggregate functions + `Having` — generating reports is effortless.

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
    .having("total", total -> ((Number) total).intValue() > 10)
    .orderBy(Order::getPrice)
    .toList(CategoryStats.class);
```

### Output

Query results map to objects, `List`, `Map`, or a single value — plus lightweight terminals.

```java
List<User> list = Linq.from(source).toList(User.class);   // List<T>
User one        = Linq.from(source).eq(User::getId, 1).one(User.class);  // exactly one (throws if >1)
User first      = Linq.from(source).orderBy(User::getId).first(User.class); // first or null
List<Map<String, Object>> maps = Linq.from(source).toMaps(); // List<Map>
Map<String, Object> map        = Linq.from(source).toMap();  // single Map
int total       = Linq.from(source).gt(User::getAge, 18).count();    // row count
boolean any     = Linq.from(source).eq(User::getName, "root").exists(); // any match?
```

## 🗂️ Supported Data Sources

Linq.J operates on any **in-memory collection**. Feed it data from anywhere:

| Source | Usage |
|--------|-------|
| List / Array | `Linq.from(list)` or `Linq.from("A", "B", "C")` |
| SQL Result | Load via JDBC / MyBatis / JPA, then query with Linq |
| CSV / XML / JSON | Parse into objects, then query with Linq |
| Redis / MongoDB | Fetch results, then query with Linq |
| Stream / File | Collect into a list, then query with Linq |
| RPC Response | Feign / Dubbo / gRPC results, then query with Linq |

## 🏗️ Architecture

A clean **layered design** — query building and execution are decoupled and independently extensible:

```
┌───────────────────────────────────────────────────────┐
│                    Fluent API Layer                    │
│    Select · Join · Where · GroupBy · OrderBy · Write   │
├───────────────────────────────────────────────────────┤
│                    Query Model Layer                   │
│         Linq (Facade) · Dql (State) · Column · Row     │
├───────────────────────────────────────────────────────┤
│                  Execution Engine Layer                │
│        Engine (Abstract) → EruptEngine (Default)       │
├───────────────────────────────────────────────────────┤
│                  Lambda Resolution Layer               │
│       SFunction · LambdaSee · SerializedLambda         │
└───────────────────────────────────────────────────────┘
```

**Pluggable Engine** — supply your own execution strategy per query:

```java
Linq linq = Linq.from(data);
linq.setEngine(new MyCustomEngine());
linq.where(User::getAge, age -> age >= 18).toList(User.class);
```

**Parallel materialization** — opt in for large datasets (order-preserving, identical results):

```java
Linq.from(bigList).parallel()
    .where(User::getAge, age -> age >= 18)
    .toList(User.class);
```

## 🎯 Use Cases

| Scenario | Description |
|----------|-------------|
| **RPC Result Association** | Join results from Feign / Dubbo / gRPC calls in memory instead of multiple DB roundtrips |
| **Heterogeneous Data** | Unify and query data from Redis, MongoDB, MySQL in one place |
| **Post-SQL Processing** | Further filter, sort, and aggregate database query results in code |
| **In-Memory Pagination** | Merge multiple result sets, then sort, aggregate, and paginate |
| **Object Mapping** | Semantic, readable object transformation and projection |
| **Cross-Source Federation** | Federated queries across different data sources at the application layer |

## ⚖️ Before & After

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
    .toList(Stats.class);
```

</td>
</tr>
</table>

## 🗺️ Roadmap

- [x] `HAVING` clause support
- [x] Group column formatting (`group by date(created_at)`)
- [ ] Set operations: `UNION ALL`, `UNION`, `INTERSECT`, `EXCEPT`, `UNION BY NAME`
- [ ] Window functions (`ROW_NUMBER`, `RANK`, `DENSE_RANK`, …)

## 🤝 Contributing

Contributions are welcome — feel free to open issues and pull requests.

## 📄 License

[MIT](./LICENSE) © Linq.J
