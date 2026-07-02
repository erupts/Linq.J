package xyz.erupt.linq.lambda;

import xyz.erupt.linq.exception.LinqException;

import java.beans.Introspector;
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
        // A non-capturing method reference (Class::method) is a call-site singleton, so the
        // cache hits on repeated calls from the same site and its size is bounded by the number
        // of distinct call sites. computeIfAbsent gives us a single atomic lookup + parse.
        return S_FUNCTION_CACHE.computeIfAbsent(func, LambdaSee::parse);
    }

    private static LambdaInfo parse(SFunction<?, ?> func) {
        try {
            if (!func.getClass().isSynthetic())
                throw new LinqException("Synthetic classes produced by non-lambda expressions");
            Method method = func.getClass().getDeclaredMethod(WRITE_REPLACE);
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            Matcher matcher = CLASS_TYPE_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
            if (!matcher.find() || matcher.groupCount() != 1)
                throw new RuntimeException("Failed to get Lambda information");
            Class<?> clazz = Class.forName(matcher.group(1).replace("/", "."));
            return getserializedLambdaInfo(serializedLambda, clazz);
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
            return new LambdaInfo(clazz, methodName, Introspector.decapitalize(field));
        }
    }

}
