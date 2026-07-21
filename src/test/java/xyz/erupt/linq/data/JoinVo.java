package xyz.erupt.linq.data;

public class JoinVo {

    private Integer orderId;

    private Double amount;

    private String name;

    public Integer getOrderId() {
        return orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setName(String name) {
        this.name = name;
    }
}
