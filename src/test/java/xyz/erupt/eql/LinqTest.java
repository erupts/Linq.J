package xyz.erupt.eql;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.eql.data.TestSource;
import xyz.erupt.eql.data.TestTarget;
import xyz.erupt.eql.grammar.OrderBy;
import xyz.erupt.eql.util.Columns;

import java.util.*;

public class LinqTest {

    private final List<TestSource> source = new ArrayList<>();

    private final List<TestTarget> target = new ArrayList<>();


    @Before
    public void before() {
        source.add(new TestSource(1, "Thanos", new Date(), new String[]{"a", "b"}));
        source.add(new TestSource(2, "Cube", new Date(), new String[]{"c", "d"}));
        source.add(new TestSource(3, "Berg", new Date(), new String[]{"f"}));
        source.add(new TestSource(4, "Liz", new Date(), new String[]{"x", "y", "z"}));

        target.add(new TestTarget(1, "Aa"));
        target.add(new TestTarget(2, "Bb"));
        target.add(new TestTarget(3, "Cc"));
    }

    @Test
    public void joinTest() {
        List<Map<String, Object>> leftJoinRes = Linq.from(source)
                .leftJoin(target, TestTarget::getAge, TestSource::getAge)
                .select(
                        Columns.all(TestSource.class),
                        Columns.of(TestTarget::getName, "name2")
                )
                .writeMap();
        assert source.size() == leftJoinRes.size();
        assert leftJoinRes.get(0).get("name2") != null;

        List<Map<String, Object>> rightJoinRes = Linq.from(source)
                .rightJoin(target, TestTarget::getAge, TestSource::getAge)
                .select(
                        Columns.all(TestSource.class),
                        Columns.of(TestTarget::getName, "name2")
                )
                .writeMap();
        assert target.size() == rightJoinRes.size();

    }

    @Test
    public void writeSimpleType() {
        List<String[]> arrays = Linq.from(source).select(Columns.of(TestSource::getTags)).write(String[].class);
        List<Date> dates = Linq.from(source).select(Columns.of(TestSource::getAge)).write(Date.class);
        List<Integer> integers = Linq.from(source).select(Columns.of(TestSource::getAge)).write(Integer.class);
        List<String> strings = Linq.from(source).select(Columns.of(TestSource::getName)).write(String.class);
        assert !arrays.isEmpty() && !dates.isEmpty() && !integers.isEmpty() && !strings.isEmpty();
    }


    @Test
    public void groupBy() {
        List<Map<String, Object>> result = Linq.from(source)
                .groupBy(Columns.of(TestSource::getName))
                .select(
                        Columns.of(TestSource::getAge, "age_xxx"),
                        Columns.sum(TestSource::getAge, TestTarget::getAge),
                        Columns.min(TestSource::getAge, TestSource::getDate),
                        Columns.avg(TestSource::getAge, "avg"),
                        Columns.count(TestSource::getName, "ncount")
                ).writeMap();
        System.out.println(result);
    }

    @Test
    public void orderBy() {
        List<TestSource> result = Linq.from(source)
                .select(Columns.all(TestSource.class))
                .orderBy(TestSource::getAge, OrderBy.Direction.DESC)
                .orderBy(TestSource::getName)
                .write(TestSource.class);
        source.sort(Comparator.comparingInt(TestSource::getAge));
        assert Objects.equals(result.get(0).getAge(), source.get(source.size() - 1).getAge());
    }

    @Test
    public void customerCondition() {
        List<Map<String, Object>> res = Linq.from(source)
                .innerJoin(target, TestTarget::getAge, TestSource::getAge)
                .select(
                        Columns.all(TestSource.class),
                        Columns.of(TestTarget::getName, "name2"),
                        Columns.sum(TestSource::getAge, "sum")
                )
                .condition(data -> {
                    Object o = data.get(Columns.fromLambda(TestSource::getTags));
                    if (null != o) {
                        return ((String[]) o)[0].equals("a");
                    }
                    return true;
                })
                .writeMap();
        System.out.println(res);
    }


    @Test
    public void conditionEqTest() {
        Linq.from(source).ne(TestSource::getName, 1).write(null);
    }

    @Test
    public void conditionInTest() {
        Linq.from(source).in(TestSource::getAge, 1, null).write(null);
    }

    @Test
    public void conditionNotInTest() {
        Linq.from(source).notIn(TestSource::getAge, 1, 2, null).write(null);
    }

    @Test
    public void conditionLikeTest() {
        Linq.from(source).like(TestSource::getName, 'a').write(null);
    }

    @Test
    public void conditionLtTest() {
        Linq.from(source).gt(TestSource::getName, "bb").write(null);
    }

}
