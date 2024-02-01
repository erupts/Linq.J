package xyz.erupt.linq;

import org.junit.Test;
import xyz.erupt.linq.data.TestSource;
import xyz.erupt.linq.lambda.LambdaReflect;

public class LambdaTest {

    @Test
    public void testLambdaInfo() {
        assert "id".equals(LambdaReflect.getInfo(TestSource::getId).getField());
        assert "name".equals(LambdaReflect.getInfo(TestSource::getName).getField());
        assert TestSource.class == LambdaReflect.getInfo(TestSource::getName).getClazz();
    }

    @Test
    public void lambdaCacheTest() {
        for (int i = 0; i < 1000000; i++) {
            LambdaReflect.getInfo(TestSource::getName);
            LambdaReflect.getInfo(TestSource::name);
            LambdaReflect.getInfo(TestSource::getId);
            LambdaReflect.getInfo(TestSource::getDate);
            LambdaReflect.getInfo(TestSource::getClass);
        }
        LambdaReflect.getInfo(TestSource::getClass);
    }

}
