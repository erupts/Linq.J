package xyz.erupt.linq.data;

public class NumTo {

    private String category;

    private Integer qty;

    private Double price;

    public NumTo() {
    }

    public NumTo(String category, Integer qty, Double price) {
        this.category = category;
        this.qty = qty;
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public Integer getQty() {
        return qty;
    }

    public Double getPrice() {
        return price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
