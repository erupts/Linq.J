package xyz.erupt.eql.lambda;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LambdaReflect {

    private static final String GET = "get";

    private static final String IS = "is";

    private static final Map<SFunction<?, ?>, LambdaInfo> FUNCTION_CACHE = new HashMap<>();

    public static <T, R> LambdaInfo getInfo(SFunction<T, R> func) {
        try {
            if (FUNCTION_CACHE.containsKey(func)) return FUNCTION_CACHE.get(func);
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String fieldName = serializedLambda.getImplMethodName();
            if (fieldName.startsWith(GET)) fieldName = fieldName.substring(GET.length());
            if (fieldName.startsWith(IS)) fieldName = fieldName.substring(IS.length());
            LambdaInfo lambdaInfo = new LambdaInfo(
                    Class.forName(serializedLambda.getImplClass().replace("/", ".")),
                    fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1)
            );
            FUNCTION_CACHE.put(func, lambdaInfo);
            return lambdaInfo;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
