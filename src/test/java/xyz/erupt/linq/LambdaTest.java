package xyz.erupt.linq;

import org.junit.Test;
import xyz.erupt.linq.data.source.TestSource;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.Th;

import java.math.BigDecimal;

public class LambdaTest {

    @Test
    public void testLambdaInfo() {
        assert "id".equals(LambdaSee.info(TestSource::getId).getField());
        assert "name".equals(LambdaSee.info(TestSource::getName).getField());
        assert "getName".equals(LambdaSee.info(TestSource::getName).getMethod());
        assert "is".equals(LambdaSee.info(Th::is).getField());
        assert "is".equals(LambdaSee.info(Th::is).getMethod());
        assert TestSource.class == LambdaSee.info(TestSource::getName).getClazz();
    }

    @Test
    public void lambdaCacheTest() {
        for (int i = 0; i < 1000000; i++) {
            LambdaSee.info(TestSource::getName);
            LambdaSee.info(TestSource::name);
            LambdaSee.info(TestSource::getId);
            LambdaSee.info(TestSource::getDate);
        }
    }
    
    @Test
    public void isAssignableFrom(){
        assert Number[].class.isAssignableFrom(Byte[].class);
        assert Number[].class.isAssignableFrom(Integer[].class);
        assert Number.class.isAssignableFrom(BigDecimal.class);
    }

}
