package xyz.erupt.linq.data;

public class JoinOrder {

    private Integer orderId;

    private Integer userId;

    private Double amount;

    public JoinOrder() {
    }

    public JoinOrder(Integer orderId, Integer userId, Double amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Double getAmount() {
        return amount;
    }
}
