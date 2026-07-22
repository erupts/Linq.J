package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.FluentTo;
import xyz.erupt.linq.data.JoinVo;
import xyz.erupt.linq.data.NoSetterTo;
import xyz.erupt.linq.data.TestTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Projection shapes: select forms (fields / class / alias / expr / exclude), distinct, and
 * the write-out setter paths — public setter, Field.set fallback (no setter), and a fluent
 * setter whose return value must be dropped by LambdaMetafactory.
 *
 * @author YuePeng
 */
public class SelectProjectionTest {

    private List<TestTo> data;

    @Before
    public void before() {
        data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            data.add(new TestTo(i, "n" + i));
        }
    }

    @Test
    public void selectClassProjectsAllFields() {
        List<TestTo> result = Linq.from(data).select(TestTo.class).toList(TestTo.class);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals("n2", result.get(2).getName());
    }

    @Test
    public void selectFieldsSubset() {
        Map<String, Object> map = Linq.from(data).eq(TestTo::getId, 1).select(TestTo::getId).toMap();
        Assert.assertEquals(1, map.size());
        Assert.assertTrue(map.containsKey("id"));
    }

    @Test
    public void selectAsAlias() {
        Map<String, Object> map = Linq.from(data).eq(TestTo::getId, 1)
                .selectAs(TestTo::getName, "label").toMap();
        Assert.assertEquals("n1", map.get("label"));
    }

    @Test
    public void selectWithValueConvert() {
        Map<String, Object> map = Linq.from(data).eq(TestTo::getId, 2)
                .select(TestTo::getId, (row, id) -> "#" + id)
                .toMap();
        Assert.assertEquals("#2", map.get("id"));
    }

    @Test
    public void selectExprFromRow() {
        Map<String, Object> map = Linq.from(data).eq(TestTo::getId, 3)
                .selectExpr(row -> row.get(TestTo::getId) + ":" + row.get(TestTo::getName), "combined")
                .toMap();
        Assert.assertEquals("3:n3", map.get("combined"));
    }

    @Test
    public void selectExcludeRemovesColumn() {
        Map<String, Object> map = Linq.from(data).eq(TestTo::getId, 1)
                .select(TestTo.class)
                .selectExclude(TestTo::getName)
                .toMap();
        Assert.assertFalse(map.containsKey("name"));
        Assert.assertTrue(map.containsKey("id"));
    }

    @Test
    public void distinctRemovesDuplicates() {
        List<TestTo> dup = new ArrayList<>(data);
        dup.addAll(data);
        Assert.assertEquals(10, Linq.from(dup).select(TestTo.class).count());
        List<Integer> ids = Linq.from(dup).select(TestTo::getId).distinct().toList(Integer.class);
        Assert.assertEquals(5, ids.size());
    }

    @Test
    public void duplicateAliasLastWins() {
        // selecting the same alias twice keeps the last definition
        Map<String, Object> map = Linq.from(data).eq(TestTo::getId, 1)
                .selectAs(TestTo::getId, "v")
                .selectAs(TestTo::getName, "v")
                .toMap();
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("n1", map.get("v"));
    }

    // no public setter -> Field.set fallback must still populate the object
    @Test
    public void writeOutViaFieldFallback() {
        List<NoSetterTo> src = new ArrayList<>();
        src.add(new NoSetterTo(7, "seven"));
        NoSetterTo out = Linq.from(src).select(NoSetterTo::getId, NoSetterTo::getName)
                .distinct() // force Row path
                .one(NoSetterTo.class);
        Assert.assertEquals(Integer.valueOf(7), out.getId());
        Assert.assertEquals("seven", out.getName());
    }

    // builder-style setter returning this: value must still be written, return dropped
    @Test
    public void writeOutViaFluentSetter() {
        List<TestTo> src = new ArrayList<>();
        src.add(new TestTo(8, "eight"));
        FluentTo out = Linq.from(src).select(TestTo::getId, TestTo::getName).one(FluentTo.class);
        Assert.assertEquals(Integer.valueOf(8), out.getId());
        Assert.assertEquals("eight", out.getName());
    }

    // direct path and Row path must produce identical objects
    @Test
    public void directAndRowPathAgree() {
        List<TestTo> direct = Linq.from(data).select(TestTo::getId, TestTo::getName).toList(TestTo.class);
        List<TestTo> row = Linq.from(data).select(TestTo::getId, TestTo::getName).distinct().toList(TestTo.class);
        Assert.assertEquals(row.size(), direct.size());
        for (int i = 0; i < direct.size(); i++) {
            Assert.assertEquals(row.get(i).getId(), direct.get(i).getId());
            Assert.assertEquals(row.get(i).getName(), direct.get(i).getName());
        }
    }
}
