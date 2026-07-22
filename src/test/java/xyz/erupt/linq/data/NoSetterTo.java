package xyz.erupt.linq.data;

// No setters on purpose: exercises the Field.set fallback in RowUtil/EruptEngine.
public class NoSetterTo {

    private Integer id;

    private String name;

    public NoSetterTo() {
    }

    public NoSetterTo(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
