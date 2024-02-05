package xyz.erupt.linq;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.TestSource;
import xyz.erupt.linq.data.TestSourceExt;
import xyz.erupt.linq.grammar.OrderBy;
import xyz.erupt.linq.lambda.SE;
import xyz.erupt.linq.util.Columns;

import java.text.SimpleDateFormat;
import java.util.*;

public class LinqTest {

    private final List<TestSource> source = new ArrayList<>();

    private final List<TestSourceExt> target = new ArrayList<>();


    @Before
    public void before() {
        source.add(new TestSource(1, "Thanos", new Date(), new String[]{"a", "b"}));
        source.add(new TestSource(2, "Cube", new Date(), new String[]{"c", "d"}));
        source.add(new TestSource(3, "Berg", new Date(), new String[]{"f"}));
        source.add(new TestSource(4, "Liz", new Date(), new String[]{"a", "s", "x", "y", "z"}));
        source.add(new TestSource(null, "Thanos", new Date(), new String[]{"k"}));

        target.add(new TestSourceExt(1, "Aa"));
        target.add(new TestSourceExt(2, "Bb"));
        target.add(new TestSourceExt(3, "Cc"));
    }

    @Test
    public void fromSingleton() {
        String name = Linq.from(new TestSource(1, "Thanos", null, null))
                .select(Columns.of(TestSource::getName)).writeOne(String.class);
        System.out.println(name);
    }

    @Test
    public void fromSimpleClass() {
        List<String> strings = new ArrayList<>();
        strings.add("A");
        strings.add("B");
        strings.add("B");
        strings.add("C");
        List<String> result = Linq.from(strings).select(Columns.of(SE::LF)).write(String.class);
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        List<Integer> n = Linq.from(numbers).select(Columns.of(SE::LF)).write(Integer.class);
        System.out.println(result);
        System.out.println(n);
    }

    @Test
    public void customerSelect() {
        List<String> result = Linq.from(source).select(
                Columns.ofs(it -> it.get("name") + " Borg", "Hello")
        ).distinct().write(String.class);
        assert result.get(0).equals(source.get(0).getName() + " Borg");
    }

    @Test
    public void joinTest() {
        List<Map<String, Object>> leftJoinRes = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(
                        Columns.all(TestSource.class),
                        Columns.of(TestSourceExt::getName, "name2")
                )
                .writeMap();
        assert source.size() == leftJoinRes.size();

        List<Map<String, Object>> rightJoinRes = Linq.from(source)
                .rightJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(
                        Columns.all(TestSource.class),
                        Columns.of(TestSourceExt::getName, "name2")
                )
                .writeMap();
        assert target.size() == rightJoinRes.size();

    }

    @Test
    public void writeSimpleType() {
        List<String[]> arrays = Linq.from(source).select(Columns.of(TestSource::getTags)).write(String[].class);
        List<Date> dates = Linq.from(source).select(Columns.of(TestSource::getId)).write(Date.class);
        List<Integer> integers = Linq.from(source).select(Columns.of(TestSource::getId)).write(Integer.class);
        List<String> strings = Linq.from(source).select(Columns.of(TestSource::getName)).write(String.class);
        assert !arrays.isEmpty() && !dates.isEmpty() && !integers.isEmpty() && !strings.isEmpty();
    }


    @Test
    public void groupBy() {
        List<Map<String, Object>> result = Linq.from(source)
                .groupBy(Columns.of(TestSource::getName))
                .select(
                        Columns.of(TestSource::getName, "name"),
                        Columns.of(TestSource::getId, "id"),
                        Columns.min(TestSource::getDate, "date"),
                        Columns.avg(TestSource::getId, "avg"),
                        Columns.count(TestSource::getName, "count"),
                        Columns.count("aCount")
                ).orderBy(TestSource::getName).writeMap();
        System.out.println(result);
    }

    @Test
    public void groupByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(dateFormat.format(new Date()));
        List<Map<String, Object>> result = Linq.from(source)
                .select(Columns.ofs(row -> dateFormat.format(row.get(TestSource::getDate)), "aa"))
//                .groupBy(Columns.ofs(row -> dateFormat.format(row.get(TestSource::getDate)), "aa"))
                .writeMap();
        System.out.println(result);
    }

    @Test
    public void orderBy() {
        List<TestSource> result = Linq.from(source)
                .select(Columns.all(TestSource.class))
                .orderBy(TestSource::getId, OrderBy.Direction.DESC)
                .orderBy(TestSource::getName)
                .write(TestSource.class);
        source.sort((a, b) -> {
            if (a.getId() == null || b.getId() == null) {
                return 0;
            }
            return b.getId() - a.getId();
        });
        assert Objects.equals(result.get(0).getId(), source.get(0).getId());
    }

    @Test
    public void customerCondition() {
        List<Map<String, Object>> res = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId).select(Columns.all(TestSource.class))
                .condition(data -> {
                    Object o = data.get(Columns.fromLambda(TestSource::getTags));
                    if (null != o) {
                        for (String s : ((String[]) o)) {
                            if ("a".equals(s)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                }).writeMap();
        int count = 0;
        for (TestSource testSource : source) {
            if (null != testSource.getTags()) {
                for (String s : testSource.getTags()) {
                    if ("a".equals(s)) {
                        count++;
                        break;
                    }
                }
            }
        }
        assert res.size() == count;
    }


    @Test
    public void conditionTest() {
        Linq.from(source).eq(TestSource::getName, "Thanos").select(Columns.of(TestSource::getId)).write(Integer.class);
        Linq.from(source).ne(TestSource::getName, "Thanos").select(Columns.of(TestSource::getId)).write(Integer.class);
        Linq.from(source).in(TestSource::getId, 1, null).select(Columns.of(TestSource::getId)).write(Integer.class);
        Linq.from(source).notIn(TestSource::getId, 1, 2, null).select(Columns.of(TestSource::getId)).write(Integer.class);
        Linq.from(source).like(TestSource::getName, "a").select(Columns.of(TestSource::getId)).write(Integer.class);
        Linq.from(source).between(TestSource::getId, 1, 3).select(Columns.of(TestSource::getId)).write(Integer.class);
    }

    @Test
    public void distinctTest() {
        List<String> names = Linq.from(source).select(Columns.of(TestSource::getName)).distinct().write(String.class);
        assert names.size() == source.stream().map(TestSource::getName).distinct().count();
    }

}
