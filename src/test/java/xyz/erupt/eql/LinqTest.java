package xyz.erupt.eql;

import org.junit.Test;
import xyz.erupt.eql.data.Master;
import xyz.erupt.eql.data.Table2;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.fun.LambdaReflect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LinqTest {

    @Test
    public void simple() {
        List<Table2> result = new ArrayList<>();

        List<Master> master = new ArrayList<>();
        List<Table2> t2 = new ArrayList<>();
        List<Table2> t3 = new ArrayList<>();

        Linq.from(master)
                .leftJoin(t2, Table2::getAge, Master::getAge)
                .between(Table2::getAge, new Date(), new Date())
                .ne(Table2::getAge, 1)
                .groupBy(Column.of(Master::getAge), Column.of(Master::getName))
                .having()
                .orderBy(Master::getAge)
                .select(
                        Column.max(Master::getAge, "max"),
                        Column.min(Table2::getName, "min"),
                        Column.count(Table2::getName, "count"),
                        Column.of(Master::getAge),
                        Column.ofs(m -> m.get("xx"), "xxx"),
                        Column.all(Master.class)
                )
                .distinct()
                .limit(10)
                .offset(10)
                .write(result);
        ;
    }

    @Test
    public void testLambdaInfo() {
        assert "age".equals(LambdaReflect.getInfo(Master::getAge).getField());
        assert "name".equals(LambdaReflect.getInfo(Master::getName).getField());
        assert Master.class == LambdaReflect.getInfo(Master::getName).getClazz();
    }

}
