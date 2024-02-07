package xyz.erupt.linq.data.source;

import java.util.Date;
import java.util.List;

public class TestSourceGroupByVo {

    private String name;

    private List<Integer> ids;

    private Date max;

    private Date min;

    private Double avg;

    private Integer count;

    private Integer nameCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public Date getMax() {
        return max;
    }

    public void setMax(Date max) {
        this.max = max;
    }

    public Date getMin() {
        return min;
    }

    public void setMin(Date min) {
        this.min = min;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getNameCount() {
        return nameCount;
    }

    public void setNameCount(Integer nameCount) {
        this.nameCount = nameCount;
    }
}
