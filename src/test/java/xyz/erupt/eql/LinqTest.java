package xyz.erupt.eql;

import org.junit.Test;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.data.Master;
import xyz.erupt.eql.data.Table2;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.lambda.LambdaReflect;
import xyz.erupt.eql.util.Columns;

import java.util.*;

public class LinqTest {

    @Test
    public void simple() {
        List<Master> master = new ArrayList<>();
        List<Table2> table = new ArrayList<>();
        List<Master> result = Linq.from(master)
                .leftJoin(table, Table2::getAge, Master::getAge)
                .join(JoinMethod.LEFT, table, (l, r) -> l.getName().equals(r.get(Columns.of(Master::getAge))))
                .isNull(Table2::getAge)
                .groupBy(Columns.of(Master::getAge), Columns.of(Master::getName))
                .having()
                .orderBy(Master::getAge)
                .select(
                        Columns.max(Master::getAge, "max"),
                        Columns.min(Table2::getName, "min"),
                        Columns.count(Table2::getName, "count"),
                        Columns.ofs(m -> m.get("xx"), "xxx"),
                        Columns.all(Master.class)
                )
                .distinct()
                .limit(10)
                .offset(10)
                .write(Master.class);
        System.out.println(result);
    }

    @Test
    public void testLambdaInfo() {
        assert "age".equals(LambdaReflect.getInfo(Master::getAge).getField());
        assert "name".equals(LambdaReflect.getInfo(Master::getName).getField());
        assert Master.class == LambdaReflect.getInfo(Master::getName).getClazz();
    }

}
