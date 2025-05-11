package org.example.dto;

import java.math.BigDecimal;

public class PaymentMethod {
    private String id;
    private double discount;
    private BigDecimal limit;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    
    public BigDecimal getLimit() {
        return limit;
    }
    
    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }
}
