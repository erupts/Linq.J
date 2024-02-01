package xyz.erupt.linq.data;

public class CustomerInfo {

    private Long customerId;

    private String nickName;

    public CustomerInfo(Long customerId, String nickName) {
        this.customerId = customerId;
        this.nickName = nickName;
    }

    public CustomerInfo() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
