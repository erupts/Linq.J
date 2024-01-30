package xyz.erupt.eql;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.eql.data.Master;
import xyz.erupt.eql.data.Table2;
import xyz.erupt.eql.grammar.OrderBy;
import xyz.erupt.eql.lambda.LambdaReflect;
import xyz.erupt.eql.util.Columns;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LinqTest {

    private final List<Master> source = new ArrayList<>();

    private final List<Table2> target = new ArrayList<>();


    @Before
    public void before() {
        source.add(new Master(2, "bb", new Date(), new String[]{"a", "b"}));
        source.add(new Master(4, "cc", new Date(), new String[]{"aa", "bb"}));
        source.add(new Master(5, "cc", new Date(), new String[]{"aaa", "bbb"}));
        source.add(new Master(6, "cc", new Date(), new String[]{"ccc", "ccc"}));
//        source.add(new Master(null, null, new Date()));

        target.add(new Table2(1, "11"));
        target.add(new Table2(2, "22"));
        target.add(new Table2(99, "99"));
    }

    @Test
    public void joinTest() {
        List<Map<String, Object>> res = Linq.from(source)
                .innerJoin(target, Table2::getAge, Master::getAge)
                .select(
                        Columns.all(Master.class),
                        Columns.of(Table2::getName, "t2"),
                        Columns.sum(Master::getAge, "sum")
                )
                .condition(data -> {
                    Object o = data.get(Columns.fromLambda(Master::getTags));
                    if (null != o) {
                        return ((String[]) o)[0].equals("a");
                    }
                    return true;
                })
                .writeMap();
        System.out.println(res);
    }

    @Test
    public void simple() {
        List<Master> result = Linq.from(source)
//                .leftJoin(target, Table2::getAge, Master::getAge)
//                .innerJoin(target, Table2::getAge, Master::getAge)
//                .join(JoinMethod.LEFT, target, (l, r) -> l.getName().equals(r.get(Columns.of(Master::getAge))))
//                .eq(Table2::getName, "a")
//                .gt(Table2::getAge, 1)
//                .groupBy(Columns.of(Master::getName))
//                .having()
                .orderBy(Master::getAge)
                .select(
                        Columns.count(Master::getAge, "count"),
                        Columns.countDistinct(Master::getAge, "countDistinct"),
                        Columns.sum(Master::getAge, "sum"),
                        Columns.max(Master::getAge, "max"),
                        Columns.min(Master::getAge, "min"),
//                        Columns.count(Table2::getName, "count"),
//                        Columns.ofs(m -> m.get("xx"), "xxx"),
                        Columns.all(Master.class)
                )
                .distinct()
                .limit(9)
                .offset(0)
                .write(Master.class);
        System.out.println(result);
    }

    @Test
    public void writeSimpleType() {
        List<String[]> arrays = Linq.from(source).select(Columns.of(Master::getTags)).write(String[].class);
        List<Date> dates = Linq.from(source).select(Columns.of(Master::getAge)).write(Date.class);
        List<Integer> integers = Linq.from(source).select(Columns.of(Master::getAge)).write(Integer.class);
        List<String> strings = Linq.from(source).select(Columns.of(Master::getName)).write(String.class);
        assert !arrays.isEmpty() && !dates.isEmpty() && !integers.isEmpty() && !strings.isEmpty();
    }


    @Test
    public void groupBy() {
        List<Map<String, Object>> result = Linq.from(source)
//                .groupBy(Columns.of(Master::getName))
                .select(
                        Columns.of(Master::getAge, "age_xxx"),
                        Columns.sum(Master::getAge, Table2::getAge),
                        Columns.min(Master::getAge, Master::getDate),
                        Columns.avg(Master::getAge, "avg"),
                        Columns.count(Master::getName, "ncount")
                )
                .writeMap();
        System.out.println(result);
    }

    @Test
    public void orderBy() {
        List<Master> result = Linq.from(source)
                .orderBy(Master::getAge, OrderBy.Direction.DESC)
                .orderBy(Master::getName)
                .write(Master.class);
        assert result.get(0).getAge() == 99;
    }

    @Test
    public void testLambdaInfo() {
        assert "age".equals(LambdaReflect.getInfo(Master::getAge).getField());
        assert "name".equals(LambdaReflect.getInfo(Master::getName).getField());
        assert Master.class == LambdaReflect.getInfo(Master::getName).getClazz();
    }

    @Test
    public void conditionEqTest() {
        Linq.from(source).ne(Master::getName, 1).write(null);
    }

    @Test
    public void conditionInTest() {
        Linq.from(source).in(Master::getAge, 1, null).write(null);
    }

    @Test
    public void conditionNotInTest() {
        Linq.from(source).notIn(Master::getAge, 1, 2, null).write(null);
    }

    @Test
    public void conditionLikeTest() {
        Linq.from(source).like(Master::getName, 'a').write(null);
    }

    @Test
    public void conditionLtTest() {
        Linq.from(source).gt(Master::getName, "bb").write(null);
    }

}
