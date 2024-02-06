package xyz.erupt.linq.data.source;

import java.util.Date;

public class TestSource {

    private Integer id;

    private String name;

    private Date date;

    private String[] tags;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public TestSource(Integer id, String name, Date date, String[] tags) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.tags = tags;
    }

    public TestSource() {
    }
}
