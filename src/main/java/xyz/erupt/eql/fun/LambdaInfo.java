package xyz.erupt.eql.fun;

public class LambdaInfo<T> {

    private final Class<T> clazz;

    private final String field;

    public LambdaInfo(Class<T> clazz, String field) {
        this.clazz = clazz;
        this.field = field;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getField() {
        return field;
    }
}
