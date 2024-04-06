package xyz.erupt.linq.lambda;

public class LambdaInfo {

    private final Class<?> clazz;

    private final String method;

    private final String field;

    public LambdaInfo(Class<?> clazz, String method, String field) {
        this.clazz = clazz;
        this.method = method;
        this.field = field;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethod() {
        return method;
    }

    public String getField() {
        return field;
    }

}
