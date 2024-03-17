package xyz.erupt.linq.lambda;

import xyz.erupt.linq.exception.LinqException;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LambdaReflect {

    private static final String GET = "get";

    private static final String IS = "is";

    private static final String WRITE_REPLACE = "writeReplace";

    private static final Map<SFunction<?, ?>, LambdaInfo> S_FUNCTION_CACHE = new HashMap<>();

    public static <T, R> LambdaInfo getInfo(SFunction<T, R> func) {
        try {
            if (S_FUNCTION_CACHE.containsKey(func)) {
                return S_FUNCTION_CACHE.get(func);
            } else synchronized (LambdaReflect.class) {
                if (S_FUNCTION_CACHE.containsKey(func)) return S_FUNCTION_CACHE.get(func);
            }
            Method method = func.getClass().getDeclaredMethod(WRITE_REPLACE);
            boolean accessible = method.isAccessible();
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String fieldName = serializedLambda.getImplMethodName();
            if (fieldName.startsWith(GET) && fieldName.length() != GET.length()) fieldName = fieldName.substring(GET.length());
            if (fieldName.startsWith(IS) && fieldName.length() != IS.length()) fieldName = fieldName.substring(IS.length());
            Class<?> clazz = Class.forName(serializedLambda.getImplClass().replace("/", "."));
            LambdaInfo lambdaInfo = new LambdaInfo(clazz, clazz.getDeclaredField(fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1)));
            method.setAccessible(accessible);
            S_FUNCTION_CACHE.put(func, lambdaInfo);
            return lambdaInfo;
        } catch (ReflectiveOperationException e) {
            throw new LinqException(e);
        }
    }

}
