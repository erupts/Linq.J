package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Test;
import xyz.erupt.linq.data.TestTo;
import xyz.erupt.linq.lambda.It;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The direct path (object -> object, skipping Row) must be indistinguishable from the Row
 * pipeline. Each case runs the same query twice: once direct-eligible, once forced onto the
 * Row path via {@code distinct()} (a no-op here — every row is unique), and compares results.
 *
 * @author YuePeng
 */
public class DirectPathTest {

    private static List<TestTo> data(int n) {
        List<TestTo> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new TestTo(i, "n" + i));
        }
        return list;
    }

    @Test
    public void projectionMatchesRowPath() {
        List<TestTo> src = data(100);
        List<TestTo> direct = Linq.from(src).select(TestTo::getId, TestTo::getName).toList(TestTo.class);
        List<TestTo> viaRow = Linq.from(src).select(TestTo::getId, TestTo::getName).distinct().toList(TestTo.class);
        Assert.assertEquals(viaRow.size(), direct.size());
        for (int i = 0; i < direct.size(); i++) {
            Assert.assertEquals(viaRow.get(i).getId(), direct.get(i).getId());
            Assert.assertEquals(viaRow.get(i).getName(), direct.get(i).getName());
        }
    }

    @Test
    public void filterOffsetLimit() {
        List<TestTo> src = data(100);
        List<TestTo> result = Linq.from(src)
                .where(TestTo::getId, id -> id >= 50)
                .select(TestTo.class)
                .offset(10).limit(5)
                .toList(TestTo.class);
        Assert.assertEquals(5, result.size());
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(Integer.valueOf(60 + i), result.get(i).getId());
        }
    }

    @Test
    public void writeMapMatchesRowPath() {
        List<TestTo> src = data(50);
        List<Map<String, Object>> direct = Linq.from(src).select(TestTo::getId, TestTo::getName).toMaps();
        List<Map<String, Object>> viaRow = Linq.from(src).select(TestTo::getId, TestTo::getName).distinct().toMaps();
        Assert.assertEquals(viaRow.size(), direct.size());
        for (int i = 0; i < direct.size(); i++) {
            Assert.assertEquals(viaRow.get(i).get("id"), direct.get(i).get("id"));
            Assert.assertEquals(viaRow.get(i).get("name"), direct.get(i).get("name"));
        }
    }

    @Test
    public void simpleCastMode() {
        List<String> strings = Linq.from("C", "A", "B", "B").gt(It::self, "A").toList(String.class);
        Assert.assertEquals("[C, B, B]", strings.toString());
        List<Integer> single = Linq.from(data(10)).where(TestTo::getId, id -> id > 7).select(TestTo::getId).toList(Integer.class);
        Assert.assertEquals("[8, 9]", single.toString());
    }

    @Test
    public void nullElementsSkipped() {
        List<TestTo> src = new ArrayList<>(data(10));
        src.add(3, null);
        src.add(null);
        List<TestTo> result = Linq.from(src).select(TestTo.class).toList(TestTo.class);
        Assert.assertEquals(10, result.size());
    }

    // no select -> select *: derived from the source element type, for both simple and pojo sources
    @Test
    public void autoSelect() {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < 5; i++) ints.add(i);
        Assert.assertEquals("[3, 4]", Linq.from(ints).gt(It::self, 2).toList(Integer.class).toString());
        List<TestTo> pojos = Linq.from(data(5)).toList(TestTo.class);
        Assert.assertEquals(5, pojos.size());
        Assert.assertEquals("n4", pojos.get(4).getName());
        Assert.assertTrue(Linq.from(data(5)).where(TestTo::getId, id -> id > 3).exists());
        Assert.assertEquals(2, Linq.from(data(5)).where(TestTo::getId, id -> id >= 3).count());
        Assert.assertEquals(Integer.valueOf(0), Linq.from(data(5)).first(TestTo.class).getId());
    }

    @Test
    public void writeOneOnDirectPath() {
        List<TestTo> src = data(10);
        TestTo one = Linq.from(src).where(TestTo::getId, id -> id == 5).select(TestTo.class).one(TestTo.class);
        Assert.assertEquals(Integer.valueOf(5), one.getId());
        Assert.assertNull(Linq.from(src).where(TestTo::getId, id -> id > 99).select(TestTo.class).one(TestTo.class));
    }
}
