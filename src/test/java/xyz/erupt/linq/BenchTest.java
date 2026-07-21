package xyz.erupt.linq;

import org.junit.BeforeClass;
import org.junit.Test;
import xyz.erupt.linq.data.JoinOrder;
import xyz.erupt.linq.data.JoinUser;
import xyz.erupt.linq.data.JoinVo;
import xyz.erupt.linq.data.TestTo;
import xyz.erupt.linq.lambda.It;
import xyz.erupt.linq.util.Columns;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

/**
 * Benchmark comparing hand-written Java against Linq.J (serial and parallel) on the same
 * workloads. Not a correctness suite — each case prints a timing line:
 *
 * <pre>
 * &lt;case&gt;   java=..ms   linq=..ms (x..)   linqPar=..ms (x..)
 * </pre>
 *
 * <p>The {@code x..} multiplier is Linq time / Java time (lower is better; 1.0 == parity with
 * hand-written code). Numbers are best-of-N after warmup. Give the forked JVM a real heap or
 * the results are GC-bound:
 * {@code mvn test -Dtest=BenchTest -DargLine="-Xmx6g -XX:+UseParallelGC"}.
 *
 * @author YuePeng
 */
public class BenchTest {

    private static final int N = 1_000_000;
    private static final int WARMUP = 3;
    private static final int ROUNDS = 5;

    private static final int DIM = 1024; // distinct group / lookup keys

    private static List<TestTo> data;
    private static List<Integer> integers;
    private static List<JoinOrder> orders;   // N rows, driving side of the join
    private static List<JoinUser> users;      // small dimension table, build side

    @BeforeClass
    public static void setup() {
        data = new ArrayList<>(N);
        integers = new ArrayList<>(N);
        orders = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            data.add(new TestTo(i, "n" + (i & (DIM - 1))));
            integers.add(i);
            orders.add(new JoinOrder(i, i & (DIM - 1), (double) i));
        }
        users = new ArrayList<>(DIM);
        for (int i = 0; i < DIM; i++) {
            users.add(new JoinUser(i, "u" + i));
        }
        System.out.printf("== BenchTest  rows=%d  cores=%d ==%n", N, Runtime.getRuntime().availableProcessors());
    }

    // ---- projection: read fields -> rebuild objects (Linq's core round trip) ----
    @Test
    public void projection() {
        IntSupplier java = () -> {
            List<TestTo> out = new ArrayList<>(N);
            for (TestTo t : data) out.add(new TestTo(t.getId(), t.getName()));
            return out.size();
        };
        IntSupplier linq = () -> Linq.from(data).select(TestTo::getName, TestTo::getId).toList(TestTo.class).size();
        IntSupplier linqPar = () -> Linq.from(data).parallel().select(TestTo::getName, TestTo::getId).toList(TestTo.class).size();
        report("projection -> List<TestTo>", java, linq, linqPar);
    }

    // ---- filter + project ----
    @Test
    public void filter() {
        int pivot = N / 2;
        IntSupplier java = () -> {
            List<TestTo> out = new ArrayList<>();
            for (TestTo t : data) if (t.getId() >= pivot) out.add(new TestTo(t.getId(), t.getName()));
            return out.size();
        };
        IntSupplier linq = () -> Linq.from(data).where(TestTo::getId, id -> id >= pivot)
                .select(TestTo.class).toList(TestTo.class).size();
        IntSupplier linqPar = () -> Linq.from(data).parallel().where(TestTo::getId, id -> id >= pivot)
                .select(TestTo.class).toList(TestTo.class).size();
        report("filter(id>=N/2) -> List<TestTo>", java, linq, linqPar);
    }

    // ---- aggregation: avg(id) ----
    @Test
    public void aggregate() {
        IntSupplier java = () -> {
            long sum = 0;
            for (TestTo t : data) sum += t.getId();
            return (int) (sum / data.size());
        };
        IntSupplier linq = () -> Linq.from(data).select(Columns.avg(TestTo::getId, "avg")).one(Integer.class);
        report("aggregate avg(id)", java, linq, null);
    }

    // ---- projection -> List<Map> ----
    @Test
    public void toMap() {
        IntSupplier java = () -> {
            List<Map<String, Object>> out = new ArrayList<>(N);
            for (TestTo t : data) {
                Map<String, Object> m = new HashMap<>(2);
                m.put("id", t.getId());
                m.put("name", t.getName());
                out.add(m);
            }
            return out.size();
        };
        IntSupplier linq = () -> Linq.from(data).select(TestTo::getId, TestTo::getName).toMaps().size();
        report("select -> List<Map>", java, linq, null);
    }

    // ---- simple element list (primitives / no field mapping) ----
    @Test
    public void simpleSelect() {
        IntSupplier java = () -> integers.stream().collect(Collectors.toList()).size();
        IntSupplier linq = () -> Linq.from(integers.toArray()).select(It::self).toList(Integer.class).size();
        report("simple select (Integer)", java, linq, null);
    }

    // ---- hash join: N orders left join a small user dimension ----
    @Test
    public void join() {
        IntSupplier java = () -> {
            Map<Integer, JoinUser> index = new HashMap<>(users.size() * 2);
            for (JoinUser u : users) index.put(u.getId(), u);
            List<JoinVo> out = new ArrayList<>(N);
            for (JoinOrder o : orders) {
                JoinUser u = index.get(o.getUserId());
                JoinVo vo = new JoinVo();
                vo.setOrderId(o.getOrderId());
                vo.setAmount(o.getAmount());
                if (u != null) vo.setName(u.getName());
                out.add(vo);
            }
            return out.size();
        };
        IntSupplier linq = () -> Linq.from(orders)
                .leftJoin(users, JoinUser::getId, JoinOrder::getUserId)
                .select(JoinOrder::getOrderId, JoinOrder::getAmount)
                .select(JoinUser::getName)
                .toList(JoinVo.class).size();
        report("left join + project", java, linq, null);
    }

    // ---- group by + multi-aggregate (count, avg): the classic report workload ----
    @Test
    public void groupBy() {
        IntSupplier java = () -> {
            Map<String, long[]> acc = new HashMap<>(DIM * 2); // [0]=count, [1]=sum(id)
            for (TestTo t : data) {
                long[] a = acc.computeIfAbsent(t.getName(), k -> new long[2]);
                a[0]++;
                a[1] += t.getId();
            }
            Map<String, double[]> out = new HashMap<>(acc.size() * 2);
            for (Map.Entry<String, long[]> e : acc.entrySet()) {
                out.put(e.getKey(), new double[]{e.getValue()[0], (double) e.getValue()[1] / e.getValue()[0]});
            }
            return out.size();
        };
        IntSupplier linq = () -> Linq.from(data)
                .groupBy(TestTo::getName)
                .select(Columns.of(TestTo::getName, "name"), Columns.count("cnt"), Columns.avg(TestTo::getId, "avgId"))
                .toMaps().size();
        report("group by + count/avg", java, linq, null);
    }

    // ---- sort: order the whole dataset by a descending key ----
    @Test
    public void sort() {
        IntSupplier java = () -> {
            List<TestTo> out = new ArrayList<>(data);
            out.sort(Comparator.comparingInt(TestTo::getId).reversed());
            return out.size();
        };
        IntSupplier linq = () -> Linq.from(data).orderByDesc(TestTo::getId).toList(TestTo.class).size();
        report("order by id desc", java, linq, null);
    }

    // ---- distinct: unique values of a low-cardinality column ----
    @Test
    public void distinct() {
        IntSupplier java = () -> {
            Set<String> seen = new LinkedHashSet<>();
            for (TestTo t : data) seen.add(t.getName());
            return seen.size();
        };
        IntSupplier linq = () -> Linq.from(data).select(TestTo::getName).distinct().toList(String.class).size();
        report("distinct name", java, linq, null);
    }

    // ---- harness ----

    private static void report(String name, IntSupplier java, IntSupplier linq, IntSupplier linqPar) {
        long j = best(java);
        long l = best(linq);
        StringBuilder sb = new StringBuilder(String.format("%-32s java=%5dms   linq=%5dms (x%.2f)", name, j, l, (double) l / j));
        if (linqPar != null) {
            long p = best(linqPar);
            sb.append(String.format("   linqPar=%5dms (x%.2f)", p, (double) p / j));
        }
        System.out.println(sb);
    }

    private static long best(IntSupplier run) {
        for (int i = 0; i < WARMUP; i++) run.getAsInt();
        long best = Long.MAX_VALUE;
        for (int i = 0; i < ROUNDS; i++) {
            long t = System.nanoTime();
            int r = run.getAsInt();
            if (r < 0) throw new IllegalStateException();
            best = Math.min(best, System.nanoTime() - t);
        }
        return best / 1_000_000;
    }

}
