package xyz.erupt.linq;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.source.TestSource;
import xyz.erupt.linq.data.source.TestSourceExt;
import xyz.erupt.linq.data.source.TestSourceExt2;
import xyz.erupt.linq.grammar.OrderBy;
import xyz.erupt.linq.lambda.Th;
import xyz.erupt.linq.util.Columns;

import java.text.SimpleDateFormat;
import java.util.*;

public class LinqTest {

    private final List<TestSource> source = new ArrayList<>();

    private final List<TestSourceExt> target = new ArrayList<>();

    private final List<TestSourceExt2> target2 = new ArrayList<>();


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

        target2.add(new TestSourceExt2(1, "A"));
        target2.add(new TestSourceExt2(1, 1));
        target2.add(new TestSourceExt2(1, true));
    }

    @Test
    public void fromSingletonObject() {
        TestSource source = new TestSource(1, "Thanos", new Date(), new String[]{"x", "y", "z"});
        String name = Linq.from(source).select(Columns.of(TestSource::getName)).writeOne(String.class);
        TestSource testSourceResult = Linq.from(source).select(Columns.all(TestSource.class)).writeOne(TestSource.class);
        assert "Thanos".equals(name);
        assert source.getId().equals(testSourceResult.getId());
        assert source.getName().equals(testSourceResult.getName());
        assert source.getDate().equals(testSourceResult.getDate());
        assert Arrays.equals(source.getTags(), testSourceResult.getTags());
    }

    @Test
    public void fromSimpleClass() {
        List<String> strings = Linq.from("C", "A", "B", "B").orderBy(Th::is).write(String.class);
        List<Integer> integers = Linq.from(1, 2, 3, 7, 6, 5).write(Integer.class);
        System.out.println(strings);
        System.out.println(integers);
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
    public void selectTest() {
        List<Map<String, Object>> list = Linq.from(source)
                .select(TestSource::getId, TestSource::getName, TestSource::getDate)
                .select(Columns.of(TestSource::getTags))
                .select(Columns.of(TestSource::getTags, "tag2"))
                .writeMap();
        assert Objects.equals(source.get(0).getDate().toString(), list.get(0).get("date").toString());
        assert Objects.equals(list.get(0).get("tags"), list.get(0).get("tag2"));
    }

    @Test
    public void aggregationSelectTest() {
        Map<String, Object> map = Linq.from(source)
                .select(TestSource::getId, TestSource::getName, TestSource::getDate)
                .select(Columns.count("count"))
                .select(Columns.countDistinct(TestSource::getId, "countDistinct"))
                .select(Columns.sum(TestSource::getId, "sum"))
                .select(Columns.max(TestSource::getId, "max"))
                .select(Columns.min(TestSource::getId, "min"))
                .select(Columns.avg(TestSource::getId, "avg"))
                .writeMapOne();
        assert Integer.parseInt(map.get("count").toString()) == source.size();
        assert Integer.parseInt(map.get("countDistinct").toString()) == source.stream().filter(it -> null != it.getId()).map(TestSource::getName).distinct().count();
        assert Integer.parseInt(map.get("sum").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).sum();
        assert Integer.parseInt(map.get("max").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).max().orElse(0);
        assert Integer.parseInt(map.get("min").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).min().orElse(0);
        assert Double.parseDouble(map.get("avg").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).average().orElse(0);
    }

    @Test
    public void customerSelect() {
        List<String> result = Linq.from(source).select(Columns.ofs(it -> it.get("name") + " Borg", "Hello")).distinct().write(String.class);
        for (int i = 0; i < result.size(); i++) assert (source.get(i).getName() + " Borg").equals(result.get(i));
    }

    @Test
    public void joinTest() {
        List<Map<String, Object>> leftJoinRes = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(Columns.all(TestSource.class))
                .select(Columns.of(TestSourceExt::getName, "name2"))
                .writeMap();
        assert source.size() == leftJoinRes.size();

        List<Map<String, Object>> rightJoinRes = Linq.from(source)
                .rightJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(Columns.all(TestSource.class))
                .select(Columns.of(TestSourceExt::getName, "name2"))
                .writeMap();
        assert target.size() == rightJoinRes.size();

        List<Map<String, Object>> innerJoin = Linq.from(source)
                .innerJoin(target, TestSourceExt::getId, TestSource::getTags)
                .select(Columns.all(TestSource.class))
                .select(Columns.of(TestSourceExt::getName, "name2"))
                .writeMap();
        assert innerJoin.isEmpty();
    }

    @Test
    public void complexJoinTest() {
        List<Map<String, Object>> testSourceInfo = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .leftJoin(target2, TestSourceExt2::getId, TestSource::getId)
                .select(Columns.all(TestSource.class),
                        Columns.of(TestSourceExt::getName, "name2"),
                        Columns.of(TestSourceExt2::getValue))
                .writeMap();
        assert testSourceInfo.size() > source.size();
    }

    @Test
    public void conditionTest() {
        String countAlias = "count";
        int eq = Linq.from(source).eq(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        int ne = Linq.from(source).ne(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        int gt = Linq.from(source).gt(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        int lt = Linq.from(source).lt(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        int gte = Linq.from(source).gte(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        int lte = Linq.from(source).lte(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        int in = Linq.from(source).in(TestSource::getId, 1, 2, null).select(Columns.count(countAlias)).writeOne(Integer.class);
        int notIn = Linq.from(source).notIn(TestSource::getId, 1, 2, null).select(Columns.count(countAlias)).writeOne(Integer.class);
        int like = Linq.from(source).like(TestSource::getName, "a").select(Columns.count(countAlias)).writeOne(Integer.class);
        int between = Linq.from(source).between(TestSource::getId, 1, 3).select(Columns.count(countAlias)).writeOne(Integer.class);
        int isNull = Linq.from(source).isNull(TestSource::getId).select(Columns.count(countAlias)).writeOne(Integer.class);
        int isNotNull = Linq.from(source).isNotNull(TestSource::getId).select(Columns.count(countAlias)).writeOne(Integer.class);
        int blank = Linq.from(source).isBlank(TestSource::getName).select(Columns.count(countAlias)).writeOne(Integer.class);
        int notBlank = Linq.from(source).isNotNull(TestSource::getName).select(Columns.count(countAlias)).writeOne(Integer.class);
        assert eq == source.stream().filter(it -> it.getName().equals("Thanos")).count();
        assert ne == source.stream().filter(it -> !it.getName().equals("Thanos")).count();

    }

    @Test
    public void orConditionTest() {
        // name = Thanos or id = 4
        List<Map<String, Object>> res = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId).select(Columns.all(TestSource.class))
                .condition(data -> {
                    Object name = data.get(TestSource::getName);
                    Object id = data.get(TestSource::getId);
                    if (null != name && null != id) {
                        return name.toString().equals("Thanos") || Integer.parseInt(id.toString()) == 4;
                    }
                    return false;
                }).writeMap();
        assert res.size() == 2;
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
        source.sort((a, b) -> a.getId() == null || b.getId() == null ? 0 : b.getId() - a.getId());
        assert Objects.equals(result.get(0).getId(), source.get(0).getId());
    }

    @Test
    public void distinctTest() {
        List<String> names = Linq.from(source).select(Columns.of(TestSource::getName)).distinct().write(String.class);
        assert names.size() == source.stream().map(TestSource::getName).distinct().count();
    }

}
