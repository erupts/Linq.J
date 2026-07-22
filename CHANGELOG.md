# Changelog

## 1.1.0 — 2026-07-22

First stable major release. Focus: **API consolidation, performance rewrite, richer terminal operators**. Upgrading from 0.x involves a small set of renames — see the migration guide at the bottom.

### ✨ New Features

#### Terminal operations

A complete set of terminal operators, aligned with mainstream query libraries:

| New API | Purpose |
|---------|---------|
| `toList(Class)` | Materialize as a list of objects (replaces `write`) |
| `one(Class)` | Expect a single result; throws when more than one row (replaces `writeOne`) |
| `first(Class)` | Return the first row; auto-applies `limit(1)`; never throws on multi-row |
| `toMaps()` | Materialize as `List<Map<String,Object>>` (replaces `writeMap`) |
| `toMap()` | Expect a single map result (replaces `writeMapOne`) |
| `count()` | Number of result rows |
| `exists()` | Whether any row matches; internally uses `limit(1)` |

#### Parallel execution engine

New opt-in `ParallelEruptEngine`, backed by `ForkJoinPool.commonPool()`:

```java
Linq.from(bigList).parallel()                 // default threshold 50_000
Linq.from(bigList).parallel(10_000)           // custom threshold
```

- **Off by default** — a library must not silently seize every host CPU core.
- Falls back to the sequential path below the threshold, avoiding fork/join overhead.
- Encounter order is preserved; results are identical to the sequential engine.

#### Direct path (Row-bypass fast lane)

`Engine.queryDirect(Dql, Class)` / `queryDirectMap(Dql)`: for simple single-table queries, source objects are transformed straight into the target type, skipping the `Row` intermediate. Returns `null` when the query shape is not eligible, and callers fall back to the full pipeline automatically.

#### `SELECT *`

When no `select` is called, `Engine.preprocessor` derives the column list from the source element type (simple types use `It::self`).

#### `where` pushdown & `having` on aliases

- `where(SFunction<R,S> col, Predicate<S> cond)`: filters **directly on the source object**; rejected elements never pay the Row materialization cost.
- `having(String alias, Predicate<Object> cond)`: enables `HAVING` on aggregate aliases.

#### `It` — self reference

For simple-typed sources (`String` / `Number` / `Date`…), use `It::self` to reference the element itself:

```java
Linq.from("C", "A", "B").gt(It::self, "A").orderByDesc(It::self).toList(String.class);
```

### ⚡ Performance Rewrites

- **`Row` internals rewritten**: replaced `HashMap<Column, Object>` with `Column[] / Object[]` arrays — meaningful memory savings on large datasets; a `putDirect` path skips duplicate checks for engine-internal callers.
- **`Accessors`**: new `LambdaMetafactory`-generated getter/setter bindings, approaching direct-call speed once JIT-compiled (~3× vs. reflection); falls back to reflection when no public accessor is available.
- **`Column.equals/hashCode`**: identity check on `table`; hand-written hash to avoid the `Object[]` allocation in `Objects.hash` — constant-factor improvement on the `Row` lookup hot path.
- **`LambdaSee.info`**: switched to `computeIfAbsent`, removing the double-checked lock and a duplicate `containsKey` probe.
- **`Columns.sum` / `avg` accumulator**: integer inputs ride a primitive `long` fast path with `BigDecimal` demotion on overflow; floating point / `BigDecimal` inputs accumulate exactly. Removes per-element `new BigDecimal(...)` allocations.
- **Where pushdown**: single-table value predicates run against source objects, so rejected rows never enter Row materialization.
- **Select row reuse**: when no `rowConvert` / `groupByFun` is used and columns match the source, existing Rows are reused instead of rebuilt.
- **Result-list pre-allocation**: capped at `min(size, 10_000)` to avoid oversized upfront allocations on very large inputs.

### 💥 Breaking Changes

#### 1. Terminal operators renamed

| 0.x | 1.1 |
|-----|-----|
| `write(Class)` | `toList(Class)` |
| `writeOne(Class)` | `one(Class)` |
| `writeMap()` | `toMaps()` |
| `writeMapOne()` | `toMap()` |

#### 2. Predicate interface swap

`where` / `having` now accept `Predicate<Row>` instead of `Function<Row, Boolean>`. Most lambdas compile unchanged; method references may need adjusting.

#### 3. Join API consolidation

- `JoinMethod` enum → **`JoinType`**
- `JoinExchange` removed; new **`JoinStrategy`** enum (currently only `HASH`)
- Join parameters unified: `onL/onR/lon/ron` → **`targetOn/sourceOn`**
- Removed the `BiFunction<T, Row, Boolean>`-based nested-loop join overload
- `JoinSchema` accessors renamed accordingly: `getLon/getRon/getJoinMethod` → `getTargetOn/getSourceOn/getJoinType`

#### 4. `Select` API

`selectRowAs(...)` → **`selectExpr(...)`** (semantics: "a computed column over the whole row")

#### 5. `Linq.from(...)` simplification

Removed 9 boxed-primitive vararg overloads: `Boolean/Byte/Character/String/Short/Integer/Long/Float/Double`. Use a collection of simple types + `It::self` instead. The helper class `Th` is also removed.

#### 6. `Where.in / notIn` parameter widening

Signature changed from `List<Object>` to `Collection<?>`.

#### 7. `Columns` utility

- `Columns.groupByProcess(...)` → **`Columns.aggregate(...)`**
- Removed the deprecated `Columns.ofx` / `Columns.ofs`

#### 8. `Engine` accessor methods

`wEngine()` / `wDQL()` → **`engine()` / `dql()`**

### 🛠 Build / Publish

- Upgraded `central-publishing-maven-plugin` from `0.7.0` to `0.8.0` — fixes the deserialization error caused by Sonatype's newly-added `warnings` response field.
- Removed the broken `disable-javadoc-doclint` profile (under JDK 8, `${java.home}` points to the JRE, which has no `javadoc` executable).

### 📚 Docs / Branding

- Fresh README (bilingual EN/CN, ~430 lines of diff)
- New brand site under `docs/`: landing page, styles, scripts, i18n language switcher
- New SVG logos: `docs/logo.svg`, `docs/logo-full.svg`, `.idea/icon.svg`

### 🧪 Tests

Systematic new test suites: `AggregationTest`, `BenchTest`, `DirectPathTest`, `JoinTest`, `OrderByTest`, `SelectProjectionTest`, `TerminalTest`, `WhereTest`; plus supporting test data classes.

### 🔧 Migration Guide (0.0.9 → 1.1.0)

A batch replace covers ~90% of the migration:

```
write(         →  toList(
writeOne(      →  one(
writeMap(      →  toMaps(
writeMapOne(   →  toMap(
selectRowAs(   →  selectExpr(
JoinMethod     →  JoinType
wEngine()      →  engine()
wDQL()         →  dql()
Columns.groupByProcess  →  Columns.aggregate
```

If you relied on `Linq.from(Integer...)`-style boxed-primitive varargs, switch to the collection form:

```java
// old
Linq.from(1, 2, 3).select(Th::is).toList(Integer.class);
// new
Linq.from(Arrays.asList(1, 2, 3)).toList(Integer.class);   // select * is derived automatically
```
