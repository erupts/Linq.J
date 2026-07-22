# Changelog

## 1.1.0 — 2026-07-22

首个稳定大版本。核心目标：**API 收敛、性能重写、终端算子完善**。因涉及公开 API 重命名，从 0.x 升级需要少量替换（见文末迁移指南）。

### ✨ 新增特性

#### 终端算子（Terminal operations）

新增一整套终端算子，与主流查询库对齐：

| 新算子 | 说明 |
|-------|------|
| `toList(Class)` | 物化为对象列表（替代 `write`） |
| `one(Class)` | 期望唯一结果，多行抛异常（替代 `writeOne`） |
| `first(Class)` | 取首行，自动加 `limit(1)`，多行不抛错 |
| `toMaps()` | 物化为 `List<Map<String,Object>>`（替代 `writeMap`） |
| `toMap()` | 期望唯一 map 结果（替代 `writeMapOne`） |
| `count()` | 结果行数 |
| `exists()` | 是否存在匹配行，内部走 `limit(1)` |

#### 并行执行引擎

新增 `ParallelEruptEngine`，可选启用，基于 `ForkJoinPool.commonPool()`：

```java
Linq.from(bigList).parallel()                 // 默认阈值 50_000
Linq.from(bigList).parallel(10_000)           // 自定义阈值
```

- **默认不开启**——库不应默默吃掉宿主应用所有 CPU 核。
- 小于阈值自动回退顺序路径，避免 fork/join 反噬。
- 保序，结果与顺序引擎完全一致。

#### 直连快通道（Direct path）

`Engine.queryDirect(Dql, Class)` / `queryDirectMap(Dql)`：单表简单查询绕过 `Row` 中间态，对象直接映射目标类型；不适用时返回 `null` 自动回退完整管线。

#### `SELECT *`

未调用任何 `select` 时，`Engine.preprocessor` 会从源元素类型自动推导所有字段作为选择列（简单类型使用 `It::self`）。

#### `where` 下推与 `having` 别名

- `where(SFunction<R,S> col, Predicate<S> cond)`：**在源对象上直接过滤**，被拒行不进入 Row 物化。
- `having(String alias, Predicate<Object> cond)`：允许在聚合别名上做 `HAVING`。

#### `It` 自引用

简单类型（`String` / `Number` / `Date`…）数据源可用 `It::self` 引用元素自身：

```java
Linq.from("C", "A", "B").gt(It::self, "A").orderByDesc(It::self).toList(String.class);
```

### ⚡ 性能重写

- **`Row` 底层重写**：由 `HashMap<Column, Object>` 改为 `Column[] / Object[]` 数组存储，大数据集内存占用显著下降；提供 `putDirect` 无重复检查路径供引擎内部使用。
- **`Accessors`**：新增基于 `LambdaMetafactory` 的 getter/setter 生成器，JIT 后逼近直接调用（~3× 反射），无 public 方法时自动回落反射。
- **`Column.equals/hashCode`**：`table` 用身份比较；手写 hash 避免 `Objects.hash` 的 `Object[]` 分配——`Row` 查找热路径的常量因子改进。
- **`LambdaSee.info`**：切到 `computeIfAbsent`，移除双检锁与 `containsKey` 二次探测。
- **`Columns.sum` / `avg` 累加器**：整型走 `long` 快路径 + 溢出降级到 `BigDecimal`；浮点/`BigDecimal` 直接精确累加。移除逐元素 `new BigDecimal(...)` 分配。
- **Where 下推**：单表值谓词在源对象层执行，不再为被拒行创建 `Row`。
- **Select 复用**：无 `rowConvert` / `groupByFun` 且列与原表一致时，复用现有 Row，跳过重建。
- **列表预分配收敛**：结果集初始容量按 `min(size, 10000)` 分配，避免超大预分配 OOM。

### 💥 破坏性变更（Breaking Changes）

#### 1. 终端算子重命名

| 0.x | 1.0 |
|-----|-----|
| `write(Class)` | `toList(Class)` |
| `writeOne(Class)` | `one(Class)` |
| `writeMap()` | `toMaps()` |
| `writeMapOne()` | `toMap()` |

#### 2. 谓词接口切换

`where` / `having` 的入参由 `Function<Row, Boolean>` 改为 `Predicate<Row>`。多数 lambda 无需修改，方法引用可能需要调整。

#### 3. Join API 收敛

- `JoinMethod` 枚举 → **`JoinType`**
- `JoinExchange` 移除，新增 **`JoinStrategy`**（当前仅 `HASH`）
- Join 参数命名统一：`onL/onR/lon/ron` → **`targetOn/sourceOn`**
- 移除基于 `BiFunction<T, Row, Boolean>` 的嵌套循环 join 重载
- `JoinSchema` 字段随之改名：`getLon/getRon/getJoinMethod` → `getTargetOn/getSourceOn/getJoinType`

#### 4. `Select` API

`selectRowAs(...)` → **`selectExpr(...)`**（语义为"整行表达式列"）

#### 5. `Linq.from(...)` 精简

移除 9 个基本包装类型可变参数重载：`Boolean/Byte/Character/String/Short/Integer/Long/Float/Double`。改为使用简单类型集合 + `It::self`。同时删除辅助类 `Th`。

#### 6. `Where.in / notIn` 参数放宽

入参由 `List<Object>` 改为 `Collection<?>`。

#### 7. `Columns` 工具类

- `Columns.groupByProcess(...)` → **`Columns.aggregate(...)`**
- 删除已废弃的 `Columns.ofx` / `Columns.ofs`

#### 8. `Engine` 引擎接入方法

`wEngine()` / `wDQL()` → **`engine()` / `dql()`**

### 🛠 构建 / 发布

- `central-publishing-maven-plugin` 由 `0.7.0` 升级至 `0.8.0`，修复 Sonatype 新增 `warnings` 字段导致的反序列化错误。
- 移除失效的 `disable-javadoc-doclint` profile（在 JDK 8 下 `${java.home}` 指向 JRE，无 `javadoc`）。

### 📚 文档 / 品牌

- 全新 README（中英双语，~430 行改动）
- 新增品牌站点 `docs/`：首页、样式、脚本、i18n 语言切换
- 新增 SVG logo：`docs/logo.svg`、`docs/logo-full.svg`、`.idea/icon.svg`

### 🧪 测试

新增系统化测试套件：`AggregationTest`、`BenchTest`、`DirectPathTest`、`JoinTest`、`OrderByTest`、`SelectProjectionTest`、`TerminalTest`、`WhereTest`；配套测试数据类若干。

### 🔧 迁移指南（0.0.9 → 1.0.0）

批量替换即可覆盖 90% 迁移工作：

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

若使用了 `Linq.from(Integer...)` 等基本类型可变参数重载，请改用集合形式：

```java
// 旧
Linq.from(1, 2, 3).select(Th::is).toList(Integer.class);
// 新
Linq.from(Arrays.asList(1, 2, 3)).toList(Integer.class);   // select * 自动推导
```
