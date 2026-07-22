package xyz.erupt.linq.data;

public class JoinUser {

    private Integer id;

    private String name;

    public JoinUser() {
    }

    public JoinUser(Integer id, String name) {
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
