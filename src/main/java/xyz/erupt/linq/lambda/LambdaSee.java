package xyz.erupt.linq.lambda;

import xyz.erupt.linq.exception.LinqException;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LambdaSee {

    private static final String GET = "get", IS = "is", WRITE_REPLACE = "writeReplace";

    private static final Pattern CLASS_TYPE_PATTERN = Pattern.compile("\\(L(.*);\\).*");

    private static final Map<SFunction<?, ?>, LambdaInfo> S_FUNCTION_CACHE = new ConcurrentHashMap<>();

    public static <T, R> String field(SFunction<T, R> func) {
        return info(func).getField();
    }

    public static <T, R> String method(SFunction<T, R> func) {
        return info(func).getMethod();
    }

    public static <T, R> LambdaInfo info(SFunction<T, R> func) {
        try {
            if (S_FUNCTION_CACHE.containsKey(func)) {
                return S_FUNCTION_CACHE.get(func);
            } else synchronized (LambdaSee.class) {
                if (S_FUNCTION_CACHE.containsKey(func)) return S_FUNCTION_CACHE.get(func);
            }
            if (!func.getClass().isSynthetic())
                throw new LinqException("Synthetic classes produced by non-lambda expressions");
            Method method = func.getClass().getDeclaredMethod(WRITE_REPLACE);
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            Matcher matcher = CLASS_TYPE_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
            if (!matcher.find() || matcher.groupCount() != 1)
                throw new RuntimeException("Failed to get Lambda information");
            Class<?> clazz = Class.forName(matcher.group(1).replace("/", "."));
            LambdaInfo lambdaInfo = getserializedLambdaInfo(serializedLambda, clazz);
            S_FUNCTION_CACHE.put(func, lambdaInfo);
            return lambdaInfo;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static LambdaInfo getserializedLambdaInfo(SerializedLambda serializedLambda, Class<?> clazz) {
        String methodName = serializedLambda.getImplMethodName();
        if (clazz.isInterface()) {
            return new LambdaInfo(clazz, methodName, null);
        } else {
            String field = methodName;
            if (methodName.startsWith(GET) && methodName.length() != GET.length())
                field = methodName.substring(GET.length());
            if (methodName.startsWith(IS) && methodName.length() != IS.length())
                field = methodName.substring(IS.length());
            return new LambdaInfo(clazz, methodName, field.substring(0, 1).toLowerCase() + field.substring(1));
        }
    }

}
