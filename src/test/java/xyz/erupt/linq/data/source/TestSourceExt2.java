package xyz.erupt.linq.data.source;

public class TestSourceExt2 {

    private Integer id;

    private Object value;

    public TestSourceExt2(Integer id, Object value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
