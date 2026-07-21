package xyz.erupt.linq.data;

// Builder-style setters returning this: LambdaMetafactory must drop the return value.
public class FluentTo {

    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FluentTo setId(Integer id) {
        this.id = id;
        return this;
    }

    public FluentTo setName(String name) {
        this.name = name;
        return this;
    }
}
