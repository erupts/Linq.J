package xyz.erupt.eql.lambda;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public class LambdaReflect {

    private static final String GET = "get";

    private static final String IS = "is";


    public static <T, R> LambdaInfo<T> getInfo(SFunction<T, R> func) {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String fieldName = serializedLambda.getImplMethodName();
            if (fieldName.startsWith(GET)) {
                fieldName = fieldName.substring(GET.length());
            }
            if (fieldName.startsWith(IS)) {
                fieldName = fieldName.substring(IS.length());
            }
            return new LambdaInfo<>(
                    (Class<T>) Class.forName(serializedLambda.getImplClass().replace("/", ".")),
                    fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1)
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
