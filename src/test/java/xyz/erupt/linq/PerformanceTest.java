package xyz.erupt.linq;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.TestTo;
import xyz.erupt.linq.lambda.Th;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Performance test
 *
 * @author YuePeng
 * date 2024/4/3 22:57
 */
public class PerformanceTest {

    private final List<TestTo> testTos = new ArrayList<>();

    private final List<Integer> integers = new ArrayList<>();

    @Before
    public void before() {
        for (int i = 0; i < 100; i++) {
            testTos.add(new TestTo(i, String.valueOf((char) i)));
        }
    }

    @Test
    public void linqSelectTest() {
        Linq.from(testTos).select(TestTo::getName).write(String.class);
    }

    @Test
    public void linqSelectTestVs() {
        testTos.stream().map(TestTo::getName).collect(Collectors.toList());
    }

    @Test
    public void linqSimpleSelectTest() {
        Linq.from(integers.toArray()).select(Th::is).write(Integer.class);
    }

    @Test
    public void linqSimpleSelectTestVs() {
        integers.stream().collect(Collectors.toList());
    }


}
