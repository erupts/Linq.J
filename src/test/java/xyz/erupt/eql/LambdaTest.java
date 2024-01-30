package xyz.erupt.eql;

import org.junit.Test;
import xyz.erupt.eql.data.TestSource;
import xyz.erupt.eql.lambda.LambdaReflect;

public class LambdaTest {

    @Test
    public void testLambdaInfo() {
        assert "age".equals(LambdaReflect.getInfo(TestSource::getAge).getField());
        assert "name".equals(LambdaReflect.getInfo(TestSource::getName).getField());
        assert TestSource.class == LambdaReflect.getInfo(TestSource::getName).getClazz();
    }

}
