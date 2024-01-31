package xyz.erupt.eql;

import org.junit.Test;
import xyz.erupt.eql.data.TestSource;
import xyz.erupt.eql.lambda.LambdaReflect;

public class LambdaTest {

    @Test
    public void testLambdaInfo() {
        assert "id".equals(LambdaReflect.getInfo(TestSource::getId).getField());
        assert "name".equals(LambdaReflect.getInfo(TestSource::getName).getField());
        assert TestSource.class == LambdaReflect.getInfo(TestSource::getName).getClazz();
    }

    @Test
    public void lambdaCacheTest() {
        LambdaReflect.getInfo(TestSource::getDate);
        LambdaReflect.getInfo(TestSource::getClass);
        for (int i = 0; i < 100000; i++) {
            LambdaReflect.getInfo(TestSource::getName);
            LambdaReflect.getInfo(TestSource::getId);
        }
    }

}
