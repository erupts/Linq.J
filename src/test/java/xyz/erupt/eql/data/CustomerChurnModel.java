package xyz.erupt.eql.data;

public class CustomerChurnModel {

    private Integer RowNumber;

    private Long CustomerId;

    private String Surname;

    private Integer CreditScore;

    private String Geography;

    private String Gender;

    private Integer Age;

    private Integer Tenure;

    private Float Balance;

    private Integer NumOfProducts;

    private Boolean HasCrCard;

    private Boolean IsActiveMember;

    private Float EstimatedSalary;

    private Boolean Exited;


    public Integer getRowNumber() {
        return RowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        RowNumber = rowNumber;
    }

    public Long getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(Long customerId) {
        CustomerId = customerId;
    }

    public String getSurname() {
        return Surname;
    }

    public void setSurname(String surname) {
        Surname = surname;
    }

    public Integer getCreditScore() {
        return CreditScore;
    }

    public void setCreditScore(Integer creditScore) {
        CreditScore = creditScore;
    }

    public String getGeography() {
        return Geography;
    }

    public void setGeography(String geography) {
        Geography = geography;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public Integer getAge() {
        return Age;
    }

    public void setAge(Integer age) {
        Age = age;
    }

    public Integer getTenure() {
        return Tenure;
    }

    public void setTenure(Integer tenure) {
        Tenure = tenure;
    }

    public Float getBalance() {
        return Balance;
    }

    public void setBalance(Float balance) {
        Balance = balance;
    }

    public Integer getNumOfProducts() {
        return NumOfProducts;
    }

    public void setNumOfProducts(Integer numOfProducts) {
        NumOfProducts = numOfProducts;
    }

    public Boolean getHasCrCard() {
        return HasCrCard;
    }

    public void setHasCrCard(Boolean hasCrCard) {
        HasCrCard = hasCrCard;
    }

    public Boolean getActiveMember() {
        return IsActiveMember;
    }

    public void setActiveMember(Boolean activeMember) {
        IsActiveMember = activeMember;
    }

    public Float getEstimatedSalary() {
        return EstimatedSalary;
    }

    public void setEstimatedSalary(Float estimatedSalary) {
        EstimatedSalary = estimatedSalary;
    }

    public Boolean getExited() {
        return Exited;
    }

    public void setExited(Boolean exited) {
        Exited = exited;
    }
}
