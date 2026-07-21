package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.consts.OrderByDirection;
import xyz.erupt.linq.data.NumTo;
import xyz.erupt.linq.lambda.It;

import java.util.Arrays;
import java.util.List;

/**
 * Ordering: asc/desc, multi-key tie-breaking, direction enum, and simple-typed sources.
 *
 * @author YuePeng
 */
public class OrderByTest {

    private List<NumTo> data;

    @Before
    public void before() {
        data = Arrays.asList(
                new NumTo("B", 2, 2.0),
                new NumTo("A", 2, 1.0),
                new NumTo("A", 1, 3.0),
                new NumTo("B", 1, 4.0)
        );
    }

    @Test
    public void ascAndDesc() {
        Assert.assertEquals("[1.0, 2.0, 3.0, 4.0]",
                Linq.from(data).orderByAsc(NumTo::getPrice).select(NumTo::getPrice).toList(Double.class).toString());
        Assert.assertEquals("[4.0, 3.0, 2.0, 1.0]",
                Linq.from(data).orderByDesc(NumTo::getPrice).select(NumTo::getPrice).toList(Double.class).toString());
    }

    @Test
    public void defaultDirectionIsAsc() {
        Assert.assertEquals(
                Linq.from(data).orderByAsc(NumTo::getPrice).select(NumTo::getPrice).toList(Double.class),
                Linq.from(data).orderBy(NumTo::getPrice).select(NumTo::getPrice).toList(Double.class));
    }

    // secondary sort key breaks ties from the primary (sort keys must be in the projection,
    // since ordering runs after select)
    @Test
    public void multiKeyOrdering() {
        List<NumTo> sorted = Linq.from(data)
                .select(NumTo.class)
                .orderBy(NumTo::getCategory, OrderByDirection.ASC)
                .orderBy(NumTo::getQty, OrderByDirection.DESC)
                .toList(NumTo.class);
        // category A (qty desc: 2,1) -> prices 1.0, 3.0 ; category B (qty desc: 2,1) -> 2.0, 4.0
        List<Double> prices = Linq.from(sorted).select(NumTo::getPrice).toList(Double.class);
        Assert.assertEquals(Arrays.asList(1.0, 3.0, 2.0, 4.0), prices);
    }

    // documents the limitation: a sort key dropped by select() cannot order the result
    @Test
    public void orderByDroppedColumnIsNoOp() {
        List<Double> prices = Linq.from(data)
                .select(NumTo::getPrice)
                .orderBy(NumTo::getCategory)
                .toList(Double.class);
        // category column is gone after select -> order unchanged from source
        Assert.assertEquals(Arrays.asList(2.0, 1.0, 3.0, 4.0), prices);
    }

    @Test
    public void orderBySimpleSource() {
        Assert.assertEquals("[1, 2, 3, 5, 6, 7]",
                Linq.from(3, 1, 2, 7, 6, 5).orderBy(It::self).toList(Integer.class).toString());
        Assert.assertEquals("[7, 6, 5, 3, 2, 1]",
                Linq.from(3, 1, 2, 7, 6, 5).orderByDesc(It::self).toList(Integer.class).toString());
    }
}
