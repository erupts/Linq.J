package xyz.erupt.linq.data;

import java.util.List;

public class AggVo {

    private String category;

    private Integer cnt;

    private Integer qtySum;

    private Double priceSum;

    private Double qtyAvg;

    private Integer qtyMin;

    private Integer qtyMax;

    private Integer distinctQty;

    private List<Object> qtyList;

    private Integer range;

    public String getCategory() {
        return category;
    }

    public Integer getCnt() {
        return cnt;
    }

    public Integer getQtySum() {
        return qtySum;
    }

    public Double getPriceSum() {
        return priceSum;
    }

    public Double getQtyAvg() {
        return qtyAvg;
    }

    public Integer getQtyMin() {
        return qtyMin;
    }

    public Integer getQtyMax() {
        return qtyMax;
    }

    public Integer getDistinctQty() {
        return distinctQty;
    }

    public List<Object> getQtyList() {
        return qtyList;
    }

    public Integer getRange() {
        return range;
    }
}
