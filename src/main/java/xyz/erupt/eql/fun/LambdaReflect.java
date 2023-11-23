package xyz.erupt.eql.fun;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public class LambdaReflect {

    public static <T, R> LambdaInfo getInfo(SFunction<T, R> func) {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String fieldName = serializedLambda.getImplMethodName();
            if (fieldName.startsWith("get")) {
                fieldName = fieldName.substring(3);
            }
            if (fieldName.startsWith("is")) {
                fieldName = fieldName.substring(2);
            }
//                    Class.forName(serializedLambda.getImplMethodSignature().substring(3))
            return new LambdaInfo(
                    Class.forName(serializedLambda.getImplClass().replace("/", ".")),
                    fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1)
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
