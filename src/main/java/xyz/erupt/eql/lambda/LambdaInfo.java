package xyz.erupt.eql.lambda;

public class LambdaInfo {

    private final Class<?> clazz;

    private final String field;

    public LambdaInfo(Class<?> clazz, String field) {
        this.clazz = clazz;
        this.field = field;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getField() {
        return field;
    }

}
