package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.TestTo;
import xyz.erupt.linq.lambda.It;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Full predicate coverage. Every typed where must behave identically whether it runs through
 * the source-object pushdown or the Row pipeline — the *MatchesRowPath cases pin that down.
 *
 * @author YuePeng
 */
public class WhereTest {

    private List<TestTo> data;

    @Before
    public void before() {
        data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(new TestTo(i, "n" + i));
        }
        data.add(new TestTo(10, null));
        data.add(new TestTo(11, ""));
    }

    @Test
    public void compareOperators() {
        Assert.assertEquals(1, Linq.from(data).eq(TestTo::getId, 5).count());
        Assert.assertEquals(11, Linq.from(data).ne(TestTo::getId, 5).count());
        Assert.assertEquals(6, Linq.from(data).gt(TestTo::getId, 5).count());
        Assert.assertEquals(7, Linq.from(data).gte(TestTo::getId, 5).count());
        Assert.assertEquals(5, Linq.from(data).lt(TestTo::getId, 5).count());
        Assert.assertEquals(6, Linq.from(data).lte(TestTo::getId, 5).count());
        Assert.assertEquals(4, Linq.from(data).between(TestTo::getId, 3, 6).count());
    }

    @Test
    public void likeAndIn() {
        Assert.assertEquals(1, Linq.from(data).like(TestTo::getName, "n3").count());
        // ids 10/11 carry null/"" names, so only "n1" itself contains "n1"
        Assert.assertEquals(1, Linq.from(data).like(TestTo::getName, "n1").count());
        Assert.assertEquals(3, Linq.from(data).in(TestTo::getId, 1, 2, 3).count());
        // Collection overload accepts any element type
        Assert.assertEquals(3, Linq.from(data).in(TestTo::getId, Arrays.asList(1, 2, 3)).count());
        Assert.assertEquals(9, Linq.from(data).notIn(TestTo::getId, 1, 2, 3).count());
        Assert.assertEquals(9, Linq.from(data).notIn(TestTo::getId, Arrays.asList(1, 2, 3)).count());
    }

    @Test
    public void nullAndBlank() {
        Assert.assertEquals(1, Linq.from(data).isNull(TestTo::getName).count());
        Assert.assertEquals(11, Linq.from(data).isNotNull(TestTo::getName).count());
        Assert.assertEquals(2, Linq.from(data).isBlank(TestTo::getName).count());
        Assert.assertEquals(10, Linq.from(data).isNotBlank(TestTo::getName).count());
    }

    @Test
    public void multipleConditionsAreAnded() {
        List<TestTo> result = Linq.from(data)
                .gt(TestTo::getId, 2)
                .lt(TestTo::getId, 6)
                .ne(TestTo::getId, 4)
                .toList(TestTo.class);
        Assert.assertEquals("[3, 5]", Linq.from(result).select(TestTo::getId).toList(Integer.class).toString());
    }

    // typed where (pushdown-eligible) and row-level where (Row pipeline) must agree
    @Test
    public void pushdownMatchesRowPath() {
        List<TestTo> typed = Linq.from(data).where(TestTo::getId, id -> id % 2 == 0).toList(TestTo.class);
        List<TestTo> rowLevel = Linq.from(data).where(row -> (Integer) row.get(TestTo::getId) % 2 == 0).toList(TestTo.class);
        Assert.assertEquals(rowLevel.size(), typed.size());
        for (int i = 0; i < typed.size(); i++) {
            Assert.assertEquals(rowLevel.get(i).getId(), typed.get(i).getId());
        }
    }

    // mixing a typed where with a row-level where disables pushdown but must not change results
    @Test
    public void mixedWhereKinds() {
        List<TestTo> result = Linq.from(data)
                .gt(TestTo::getId, 1)
                .where(row -> (Integer) row.get(TestTo::getId) < 5)
                .toList(TestTo.class);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void simpleSourcePredicates() {
        Assert.assertEquals("[B, C]", Linq.from("A", "B", "C").gt(It::self, "A").toList(String.class).toString());
        Assert.assertEquals("[A]", Linq.from("A", "B", "C").eq(It::self, "A").toList(String.class).toString());
        Assert.assertEquals(2, Linq.from(1, 2, 3).in(It::self, 1, 3).count());
    }

    @Test
    public void whereOnEmptySource() {
        Assert.assertEquals(0, Linq.from(new ArrayList<TestTo>()).eq(TestTo::getId, 1).count());
    }
}
