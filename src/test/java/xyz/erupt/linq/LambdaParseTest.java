package xyz.erupt.linq;

import org.junit.Test;
import xyz.erupt.linq.data.Table;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Objects;

public class LambdaParseTest {

    @Test
    public void parseTest() {
        assert Objects.equals(LambdaSee.field(Table::getNAME), "NAME");
        assert Objects.equals(LambdaSee.field(Table::getName1), "name1");
        assert Objects.equals(LambdaSee.field(Table::getName2), "name2");
        assert Objects.equals(LambdaSee.field(Table::getAAA_BBB), "AAA_BBB");
    }

}
