package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Test;
import xyz.erupt.linq.data.TestTo;
import xyz.erupt.linq.exception.LinqException;
import xyz.erupt.linq.util.Columns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Terminal operators and their edge cases: empty / all-null sources, one() strictness,
 * first() laziness, limit/offset boundaries, and BigDecimal narrowing on write-out.
 *
 * @author YuePeng
 */
public class TerminalTest {

    private static List<TestTo> data(int n) {
        List<TestTo> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new TestTo(i, "n" + i));
        }
        return list;
    }

    @Test
    public void emptySource() {
        List<TestTo> empty = new ArrayList<>();
        Assert.assertTrue(Linq.from(empty).toList(TestTo.class).isEmpty());
        Assert.assertNull(Linq.from(empty).one(TestTo.class));
        Assert.assertNull(Linq.from(empty).first(TestTo.class));
        Assert.assertTrue(Linq.from(empty).toMaps().isEmpty());
        Assert.assertNull(Linq.from(empty).toMap());
        Assert.assertEquals(0, Linq.from(empty).count());
        Assert.assertFalse(Linq.from(empty).exists());
    }

    @Test
    public void allNullSource() {
        List<TestTo> nulls = Arrays.asList(null, null);
        Assert.assertTrue(Linq.from(nulls).toList(TestTo.class).isEmpty());
        Assert.assertFalse(Linq.from(nulls).exists());
    }

    @Test(expected = LinqException.class)
    public void oneThrowsOnMultiple() {
        Linq.from(data(3)).one(TestTo.class);
    }

    @Test(expected = LinqException.class)
    public void toMapThrowsOnMultiple() {
        Linq.from(data(3)).toMap();
    }

    @Test
    public void firstNeverThrows() {
        Assert.assertEquals(Integer.valueOf(0), Linq.from(data(5)).first(TestTo.class).getId());
        // first respects preceding stages: with orderBy it goes through the Row pipeline
        Assert.assertEquals(Integer.valueOf(4),
                Linq.from(data(5)).orderByDesc(TestTo::getId).first(TestTo.class).getId());
        // an existing tighter limit is preserved
        Assert.assertNull(Linq.from(data(5)).limit(0).first(TestTo.class));
    }

    @Test
    public void limitOffsetBoundaries() {
        Assert.assertEquals(0, Linq.from(data(5)).limit(0).count());
        Assert.assertEquals(5, Linq.from(data(5)).limit(99).count());
        Assert.assertEquals(0, Linq.from(data(5)).offset(99).count());
        Assert.assertEquals(2, Linq.from(data(5)).offset(3).count());
        Assert.assertEquals(1, Linq.from(data(5)).offset(3).limit(1).count());
    }

    @Test
    public void toMapsShape() {
        Map<String, Object> map = Linq.from(data(1)).toMap();
        Assert.assertEquals(0, map.get("id"));
        Assert.assertEquals("n0", map.get("name"));
    }

    @Test
    public void bigDecimalNarrowing() {
        // aggregates produce BigDecimal; write-out narrows to the requested type
        Assert.assertEquals(Integer.valueOf(10),
                Linq.from(data(5)).select(Columns.sum(TestTo::getId, "s")).one(Integer.class));
        Assert.assertEquals(Long.valueOf(10L),
                Linq.from(data(5)).select(Columns.sum(TestTo::getId, "s")).one(Long.class));
        Assert.assertEquals(2.0,
                Linq.from(data(5)).select(Columns.avg(TestTo::getId, "a")).one(Double.class), 1e-9);
        Assert.assertEquals("10",
                Linq.from(data(5)).select(Columns.sum(TestTo::getId, "s")).one(String.class));
    }

    @Test
    public void countAndExistsWithConditions() {
        Assert.assertEquals(2, Linq.from(data(5)).gte(TestTo::getId, 3).count());
        Assert.assertTrue(Linq.from(data(5)).eq(TestTo::getId, 4).exists());
        Assert.assertFalse(Linq.from(data(5)).eq(TestTo::getId, 99).exists());
    }
}
