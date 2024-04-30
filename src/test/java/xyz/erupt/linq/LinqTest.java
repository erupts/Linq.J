package xyz.erupt.linq;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.consts.OrderByDirection;
import xyz.erupt.linq.data.source.TestSource;
import xyz.erupt.linq.data.source.TestSourceExt;
import xyz.erupt.linq.data.source.TestSourceExt2;
import xyz.erupt.linq.data.source.TestSourceGroupByVo;
import xyz.erupt.linq.lambda.Th;
import xyz.erupt.linq.util.Columns;

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
        source.add(new TestSource(5, "Thanos", new Date(), new String[]{"k"}));
        source.add(new TestSource(null, "", new Date(), new String[]{"k"}));

        target.add(new TestSourceExt(1, "Aa"));
        target.add(new TestSourceExt(2, "Bb"));
        target.add(new TestSourceExt(3, "Cc"));

        target2.add(new TestSourceExt2(1, "A"));
        target2.add(new TestSourceExt2(1, 1));
        target2.add(new TestSourceExt2(1, true));
    }

    @Test
    public void selectId() {
        List<Long> longs = Linq.from(source).select(TestSource::getId).write(Long.class);
        System.out.println(longs);
    }

    @Test
    public void fromSingletonObject() {
        TestSource source = new TestSource(1, "Thanos", new Date(), new String[]{"x", "y", "z"});
        String name = Linq.from(source).select(TestSource::getName).writeOne(String.class);
        TestSource testSourceResult = Linq.from(source).select(TestSource.class).writeOne(TestSource.class);
        assert "Thanos".equals(name);
        assert source.getId().equals(testSourceResult.getId());
        assert source.getName().equals(testSourceResult.getName());
        assert source.getDate().equals(testSourceResult.getDate());
        assert Arrays.equals(source.getTags(), testSourceResult.getTags());
    }

    @Test
    public void fromSimpleClass() {
        List<String> strings = Linq.from("C", "A", "B", "B").gt(Th::is, "A").orderByDesc(Th::is).write(String.class);
        List<Integer> integers = Linq.from(1, 2, 3, 7, 6, 5).orderBy(Th::is).write(Integer.class);
        System.out.println(strings);
        System.out.println(integers);
    }

    @Test
    public void writeSimpleType() {
        List<String[]> arrays = Linq.from(source).select(TestSource::getTags).write(String[].class);
        List<Date> dates = Linq.from(source).select(TestSource::getId).write(Date.class);
        List<Integer> integers = Linq.from(source).select(TestSource::getId).write(Integer.class);
        List<String> strings = Linq.from(source).select(TestSource::getName).write(String.class);
        assert !arrays.isEmpty() && !dates.isEmpty() && !integers.isEmpty() && !strings.isEmpty();
    }

    @Test
    public void selectTest() {
        List<Map<String, Object>> list = Linq.from(source)
                .select(TestSource::getName, TestSource::getDate, TestSource::getTags)
                .selectAs(TestSource::getTags, "tag2")
                .select(Columns.ofx(TestSource::getId, id -> id + "xxxx"))
                .writeMap();
        assert Objects.equals(source.get(0).getDate().toString(), list.get(0).get("date").toString());
        assert Objects.equals(list.get(0).get("tags"), list.get(0).get("tag2"));
    }

    @Test
    public void selectProcessTest2() {
        List<Map<String, Object>> list = Linq.from(source)
                .select(Columns.ofx(TestSource::getId, id -> id + "xxxx"))
                .select(Columns.sum(TestSource::getId, "sum"))
                .groupBy(Columns.ofx(TestSource::getId, id -> id))
                .having(row -> Integer.parseInt(row.get("sum").toString()) > 4)
                .writeMap();
        System.out.println(list);
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
        assert Integer.parseInt(map.get("countDistinct").toString()) == source.stream().map(TestSource::getName).distinct().count();
        assert Integer.parseInt(map.get("sum").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).sum();
        assert Integer.parseInt(map.get("max").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).max().orElse(0);
        assert Integer.parseInt(map.get("min").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).min().orElse(0);
        assert Double.parseDouble(map.get("avg").toString()) == source.stream().filter(it -> null != it.getId()).mapToInt(TestSource::getId).average().orElse(0);
    }

    @Test
    public void customerSelect() {
        List<String> result = Linq.from(source).select(Columns.ofs(it -> it.get(TestSource::getName) + " Borg", "Hello")).write(String.class);
        for (int i = 0; i < result.size(); i++) {
            assert (source.get(i).getName() + " Borg").equals(result.get(i));
        }
    }

    @Test
    public void joinTest() {
        List<Map<String, Object>> leftJoinRes = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(TestSource.class)
                .selectAs(TestSourceExt::getName, "name2")
                .writeMap();
        assert source.size() == leftJoinRes.size();

        List<Map<String, Object>> rightJoinRes = Linq.from(source)
                .rightJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(TestSource.class)
                .selectAs(TestSourceExt::getName, "name2")
                .writeMap();
        assert target.size() == rightJoinRes.size();

        List<Map<String, Object>> innerJoin = Linq.from(source)
                .innerJoin(target, TestSourceExt::getId, TestSource::getTags)
                .select(TestSource.class)
                .selectAs(TestSourceExt::getName, "name2")
                .writeMap();
        assert innerJoin.isEmpty();
    }

    @Test
    public void complexJoinTest() {
        List<Map<String, Object>> testSourceInfo = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .leftJoin(target2, TestSourceExt2::getId, TestSource::getId)
                .select(TestSource.class)
                .select(TestSourceExt2::getValue)
                .selectAs(TestSourceExt::getName, "name2")
                .writeMap();
        assert testSourceInfo.size() > source.size();
    }

    @Test
    public void whereTest() {
        String countAlias = "count";
        Integer eq = Linq.from(source).eq(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer ne = Linq.from(source).ne(TestSource::getName, "Thanos").select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer gt = Linq.from(source).gt(TestSource::getId, 2).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer lt = Linq.from(source).lt(TestSource::getId, 2).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer gte = Linq.from(source).gte(TestSource::getId, 2).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer lte = Linq.from(source).lte(TestSource::getId, 2).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer between = Linq.from(source).between(TestSource::getId, 1, 3).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer in = Linq.from(source).in(TestSource::getId, 1, 2, null).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer notIn = Linq.from(source).notIn(TestSource::getId, 1, 2, null).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer like = Linq.from(source).like(TestSource::getName, "a").select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer isNull = Linq.from(source).isNull(TestSource::getId).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer isNotNull = Linq.from(source).isNotNull(TestSource::getId).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer blank = Linq.from(source).isBlank(TestSource::getName).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer notBlank = Linq.from(source).isNotBlank(TestSource::getName).select(Columns.count(countAlias)).writeOne(Integer.class);
        Integer fieldWhere = Linq.from(source).where(TestSource::getId, id -> null != id && id >= 5).select(Columns.count(countAlias)).writeOne(Integer.class);
        assert eq == source.stream().filter(it -> it.getName().equals("Thanos")).count();
        assert ne == source.stream().filter(it -> !it.getName().equals("Thanos")).count();
        assert gt == source.stream().filter(it -> null != it.getId() && it.getId() > 2).count();
        assert lt == source.stream().filter(it -> null != it.getId() && it.getId() < 2).count();
        assert gte == source.stream().filter(it -> null != it.getId() && it.getId() >= 2).count();
        assert lte == source.stream().filter(it -> null != it.getId() && it.getId() <= 2).count();
        assert between == source.stream().filter(it -> null != it.getId() && it.getId() >= 1 && it.getId() <= 3).count();
        assert in == source.stream().filter(it -> null != it.getId() && (it.getId() == 1 || it.getId() == 2)).count();
        assert notIn == source.stream().filter(it -> null != it.getId() && it.getId() != 1 && it.getId() != 2).count();
        assert like == source.stream().filter(it -> it.getName().contains("a")).count();
        assert isNull == source.stream().filter(it -> null == it.getId()).count();
        assert isNotNull == source.stream().filter(it -> null != it.getId()).count();
        assert blank == source.stream().filter(it -> null == it.getName() || it.getName().trim().isEmpty()).count();
        assert notBlank == source.stream().filter(it -> null != it.getName() && !it.getName().trim().isEmpty()).count();
        assert fieldWhere == source.stream().filter(it -> null != it.getId() && it.getId() >= 5).count();
    }

    @Test
    public void orConditionTest() {
        // name = 'Thanos' or id = 4
        List<Map<String, Object>> res = Linq.from(source)
                .leftJoin(target, TestSourceExt::getId, TestSource::getId)
                .select(TestSource.class)
                .where(data -> {
                    String name = data.get(TestSource::getName);
                    Integer id = data.get(TestSource::getId);
                    if (null != name && null != id) {
                        return name.equals("Thanos") || id == 4;
                    }
                    return false;
                }).writeMap();
        assert res.size() == 3;
    }


    @Test
    public void groupBy() {
        List<TestSourceGroupByVo> result = Linq.from(source)
                .groupBy(TestSource::getName)
                .select(
                        Columns.of(TestSource::getName, TestSourceGroupByVo::getName),
                        Columns.groupArray(TestSource::getId, TestSourceGroupByVo::getIds),
                        Columns.min(TestSource::getDate, TestSourceGroupByVo::getMin),
                        Columns.max(TestSource::getDate, TestSourceGroupByVo::getMax),
                        Columns.avg(TestSource::getId, TestSourceGroupByVo::getAvg),
                        Columns.count(TestSource::getName, TestSourceGroupByVo::getNameCount),
                        Columns.count(TestSourceGroupByVo::getCount)
                )
                .having(TestSourceGroupByVo::getAvg, avg -> avg >= 1)
                .orderBy(TestSource::getName).write(TestSourceGroupByVo.class);
        System.out.println(result);
    }

    @Test
    public void orderBy() {
        List<TestSource> result = Linq.from(source)
                .select(TestSource.class)
                .selectExclude(TestSource::getTags, TestSource::getName)
                .orderBy(TestSource::getId, OrderByDirection.DESC)
                .orderBy(TestSource::getName)
                .write(TestSource.class);
        source.sort((a, b) -> a.getId() == null || b.getId() == null ? 0 : b.getId() - a.getId());
        assert Objects.equals(result.get(0).getId(), source.get(0).getId());
    }

    @Test
    public void distinctTest() {
        List<String> names = Linq.from(source)
                .select(TestSource::getName)
                .distinct()
                .write(String.class);
        assert names.size() == source.stream().map(TestSource::getName).distinct().count();
    }

    @Test
    public void limit() {
        int offset = 3;
        String name = Linq.from(source)
                .select(TestSource::getName)
                .distinct()
                .limit(1).offset(offset)
                .writeOne(String.class);
        for (int i = 0; i < source.size(); i++) {
            assert i != offset || name.equals(source.get(i).getName());
        }
    }

}
