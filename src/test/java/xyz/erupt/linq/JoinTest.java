package xyz.erupt.linq;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.consts.JoinType;
import xyz.erupt.linq.data.JoinOrder;
import xyz.erupt.linq.data.JoinUser;
import xyz.erupt.linq.data.JoinVo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * All four join types against the same fixture, including an unmatched foreign key (order 104)
 * and a one-to-many expansion (user 1 has two orders).
 *
 * @author YuePeng
 */
public class JoinTest {

    private List<JoinUser> users;

    private List<JoinOrder> orders;

    @Before
    public void before() {
        users = Arrays.asList(new JoinUser(1, "A"), new JoinUser(2, "B"), new JoinUser(3, "C"));
        orders = Arrays.asList(
                new JoinOrder(101, 1, 10.0),
                new JoinOrder(102, 1, 20.0),
                new JoinOrder(103, 2, 30.0),
                new JoinOrder(104, 9, 40.0) // no matching user
        );
    }

    private Linq fromOrders(JoinType type) {
        return Linq.from(orders)
                .join(type, users, JoinUser::getId, JoinOrder::getUserId)
                .select(JoinOrder::getOrderId, JoinOrder::getAmount)
                .select(JoinUser::getName);
    }

    @Test
    public void leftJoinKeepsUnmatched() {
        List<JoinVo> result = fromOrders(JoinType.LEFT).toList(JoinVo.class);
        Assert.assertEquals(4, result.size());
        JoinVo orphan = result.stream().filter(it -> it.getOrderId() == 104).findFirst().get();
        Assert.assertNull(orphan.getName());
        JoinVo matched = result.stream().filter(it -> it.getOrderId() == 103).findFirst().get();
        Assert.assertEquals("B", matched.getName());
    }

    @Test
    public void innerJoinDropsUnmatched() {
        List<JoinVo> result = fromOrders(JoinType.INNER).toList(JoinVo.class);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.stream().noneMatch(it -> it.getOrderId() == 104));
    }

    @Test
    public void rightJoinIsTargetDriven() {
        // driven by users: user1 expands to two orders, user3 has none -> 4 rows
        List<JoinVo> result = fromOrders(JoinType.RIGHT).toList(JoinVo.class);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(2, result.stream().filter(it -> "A".equals(it.getName())).count());
        JoinVo noOrder = result.stream().filter(it -> "C".equals(it.getName())).findFirst().get();
        Assert.assertNull(noOrder.getOrderId());
    }

    @Test
    public void fullJoinKeepsBothSides() {
        List<JoinVo> result = fromOrders(JoinType.FULL).toList(JoinVo.class);
        Assert.assertEquals(5, result.size()); // 4 orders + userC without orders
        Assert.assertTrue(result.stream().anyMatch(it -> it.getOrderId() != null && it.getOrderId() == 104));
        Assert.assertTrue(result.stream().anyMatch(it -> "C".equals(it.getName()) && it.getOrderId() == null));
    }

    @Test
    public void oneToManyExpansion() {
        // driven by users joining orders: user1 x2, user2 x1, user3 x1
        List<JoinVo> result = Linq.from(users)
                .leftJoin(orders, JoinOrder::getUserId, JoinUser::getId)
                .select(JoinUser::getName)
                .select(JoinOrder::getOrderId, JoinOrder::getAmount)
                .toList(JoinVo.class);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(30.0, result.stream().filter(it -> "A".equals(it.getName()))
                .mapToDouble(it -> Objects.requireNonNull(it.getAmount())).sum(), 1e-9);
    }

    @Test
    public void joinWithWhereAndOrder() {
        // where runs after join in the Row pipeline
        List<JoinVo> result = fromOrders(JoinType.INNER)
                .where(JoinOrder::getAmount, a -> a >= 20.0)
                .orderByDesc(JoinOrder::getAmount)
                .toList(JoinVo.class);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(Integer.valueOf(103), result.get(0).getOrderId());
        Assert.assertEquals(Integer.valueOf(102), result.get(1).getOrderId());
    }

    @Test
    public void convenienceMethodsMatchGenericJoin() {
        Assert.assertEquals(fromOrders(JoinType.INNER).count(),
                Linq.from(orders).innerJoin(users, JoinUser::getId, JoinOrder::getUserId)
                        .select(JoinOrder::getOrderId).count());
        Assert.assertEquals(fromOrders(JoinType.LEFT).count(),
                Linq.from(orders).leftJoin(users, JoinUser::getId, JoinOrder::getUserId)
                        .select(JoinOrder::getOrderId).count());
    }
}
