package xyz.erupt.linq.data;

public class CustomerChurnModel {

    private Integer rowNumber;

    private Long customerId;

    private String surname;

    private Integer creditScore;

    private String geography;

    private String gender;

    private Integer age;

    private Integer tenure;

    private Float balance;

    private Integer numOfProducts;

    private Boolean hasCrCard;

    private Boolean isActiveMember;

    private Float estimatedSalary;

    private Boolean exited;


    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getGeography() {
        return geography;
    }

    public void setGeography(String geography) {
        this.geography = geography;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getTenure() {
        return tenure;
    }

    public void setTenure(Integer tenure) {
        this.tenure = tenure;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public Integer getNumOfProducts() {
        return numOfProducts;
    }

    public void setNumOfProducts(Integer numOfProducts) {
        this.numOfProducts = numOfProducts;
    }

    public Boolean getHasCrCard() {
        return hasCrCard;
    }

    public void setHasCrCard(Boolean hasCrCard) {
        this.hasCrCard = hasCrCard;
    }

    public Boolean getActiveMember() {
        return isActiveMember;
    }

    public void setActiveMember(Boolean activeMember) {
        isActiveMember = activeMember;
    }

    public Float getEstimatedSalary() {
        return estimatedSalary;
    }

    public void setEstimatedSalary(Float estimatedSalary) {
        this.estimatedSalary = estimatedSalary;
    }

    public Boolean getExited() {
        return exited;
    }

    public void setExited(Boolean exited) {
        this.exited = exited;
    }
}
