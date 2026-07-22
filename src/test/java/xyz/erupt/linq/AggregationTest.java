package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.AggVo;
import xyz.erupt.linq.data.NumTo;
import xyz.erupt.linq.lambda.It;
import xyz.erupt.linq.util.Columns;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Aggregation coverage: every built-in aggregate, the three having() forms, the custom
 * {@code Columns.aggregate} hook, and the NumberAccumulator's precision rules (long fast path,
 * overflow demotion, exact decimal accumulation for floating point).
 *
 * @author YuePeng
 */
public class AggregationTest {

    private List<NumTo> data;

    @Before
    public void before() {
        data = Arrays.asList(
                new NumTo("A", 1, 1.5),
                new NumTo("A", 2, 2.5),
                new NumTo("A", 2, 3.5),
                new NumTo("B", 3, 3.0)
        );
    }

    private AggVo group(String category) {
        List<AggVo> result = Linq.from(data)
                .groupBy(NumTo::getCategory)
                .select(
                        Columns.of(NumTo::getCategory, "category"),
                        Columns.count("cnt"),
                        Columns.sum(NumTo::getQty, "qtySum"),
                        Columns.sum(NumTo::getPrice, "priceSum"),
                        Columns.avg(NumTo::getQty, "qtyAvg"),
                        Columns.min(NumTo::getQty, "qtyMin"),
                        Columns.max(NumTo::getQty, "qtyMax"),
                        Columns.countDistinct(NumTo::getQty, "distinctQty"),
                        Columns.groupArray(NumTo::getQty, "qtyList"),
                        Columns.aggregate(NumTo::getQty, "range", (column, list) -> {
                            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                            for (xyz.erupt.linq.schema.Row row : list) {
                                int v = (Integer) row.get(column);
                                min = Math.min(min, v);
                                max = Math.max(max, v);
                            }
                            return BigDecimal.valueOf(max - min);
                        })
                )
                .toList(AggVo.class);
        return result.stream().filter(it -> category.equals(it.getCategory())).findFirst().get();
    }

    @Test
    public void allAggregates() {
        AggVo a = group("A");
        Assert.assertEquals(Integer.valueOf(3), a.getCnt());
        Assert.assertEquals(Integer.valueOf(5), a.getQtySum());
        Assert.assertEquals(7.5, a.getPriceSum(), 1e-9);
        Assert.assertEquals(5.0 / 3, a.getQtyAvg(), 1e-9);
        Assert.assertEquals(Integer.valueOf(1), a.getQtyMin());
        Assert.assertEquals(Integer.valueOf(2), a.getQtyMax());
        Assert.assertEquals(Integer.valueOf(2), a.getDistinctQty());
        Assert.assertEquals(Arrays.asList(1, 2, 2), a.getQtyList());
        Assert.assertEquals(Integer.valueOf(1), a.getRange());

        AggVo b = group("B");
        Assert.assertEquals(Integer.valueOf(1), b.getCnt());
        Assert.assertEquals(Integer.valueOf(3), b.getQtySum());
        Assert.assertEquals(Integer.valueOf(0), b.getRange());
    }

    @Test
    public void globalAggregateWithoutGroupBy() {
        Assert.assertEquals(Integer.valueOf(8),
                Linq.from(data).select(Columns.sum(NumTo::getQty, "s")).one(Integer.class));
        Assert.assertEquals(Integer.valueOf(4),
                Linq.from(data).select(Columns.count("c")).one(Integer.class));
    }

    @Test
    public void havingThreeForms() {
        // row-level
        Assert.assertEquals(1, Linq.from(data).groupBy(NumTo::getCategory)
                .select(Columns.of(NumTo::getCategory, "category"), Columns.count("cnt"))
                .having(row -> ((BigDecimal) row.get("cnt")).intValue() >= 3)
                .count());
        // alias-based
        Assert.assertEquals(1, Linq.from(data).groupBy(NumTo::getCategory)
                .select(Columns.of(NumTo::getCategory, "category"), Columns.count("cnt"))
                .having("cnt", v -> ((BigDecimal) v).intValue() >= 3)
                .count());
        // column-based (selected column available in the grouped row)
        Assert.assertEquals(1, Linq.from(data).groupBy(NumTo::getCategory)
                .select(Columns.of(NumTo::getCategory, "category"), Columns.count("cnt"))
                .having(NumTo::getCategory, "A"::equals)
                .count());
    }

    // integral inputs ride the long fast path and demote to BigDecimal on overflow
    @Test
    public void longOverflowDemotion() {
        BigDecimal sum = Linq.from(Long.MAX_VALUE, 1L)
                .select(Columns.sum(It::self, "s"))
                .one(BigDecimal.class);
        Assert.assertEquals(new BigDecimal("9223372036854775808"), sum);
    }

    // decimal accumulation stays exact (toString parse), no binary-double drift
    @Test
    public void decimalExactness() {
        BigDecimal sum = Linq.from(0.1, 0.2)
                .select(Columns.sum(It::self, "s"))
                .one(BigDecimal.class);
        Assert.assertEquals(new BigDecimal("0.3"), sum);
        // mixed integral + floating point
        BigDecimal mixed = Linq.from((Number) 1, 0.5)
                .select(Columns.sum(It::self, "s"))
                .one(BigDecimal.class);
        Assert.assertEquals(0, mixed.compareTo(new BigDecimal("1.5")));
    }

    @Test
    public void aggregateOnEmptySource() {
        Assert.assertEquals(0, Linq.from(Arrays.<NumTo>asList()).groupBy(NumTo::getCategory)
                .select(Columns.count("cnt")).count());
    }
}
