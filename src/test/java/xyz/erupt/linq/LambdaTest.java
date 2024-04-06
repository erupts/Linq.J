package xyz.erupt.linq;

import org.junit.Test;
import xyz.erupt.linq.data.source.TestSource;
import xyz.erupt.linq.lambda.LambdaReflect;
import xyz.erupt.linq.lambda.Th;

import java.math.BigDecimal;

public class LambdaTest {

    @Test
    public void testLambdaInfo() {
        assert "id".equals(LambdaReflect.info(TestSource::getId).getField());
        assert "name".equals(LambdaReflect.info(TestSource::getName).getField());
        assert "getName".equals(LambdaReflect.info(TestSource::getName).getMethod());
        assert "is".equals(LambdaReflect.info(Th::is).getField());
        assert "is".equals(LambdaReflect.info(Th::is).getMethod());
        assert TestSource.class == LambdaReflect.info(TestSource::getName).getClazz();
    }

    @Test
    public void lambdaCacheTest() {
        for (int i = 0; i < 1000000; i++) {
            LambdaReflect.info(TestSource::getName);
            LambdaReflect.info(TestSource::name);
            LambdaReflect.info(TestSource::getId);
            LambdaReflect.info(TestSource::getDate);
        }
    }
    
    @Test
    public void isAssignableFrom(){
        assert Number[].class.isAssignableFrom(Byte[].class);
        assert Number[].class.isAssignableFrom(Integer[].class);
        assert Number.class.isAssignableFrom(BigDecimal.class);
    }

}
