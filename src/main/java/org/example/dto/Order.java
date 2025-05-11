package org.example.dto;

import java.math.BigDecimal;
import java.util.List;

public class Order {
    private String id;
    private BigDecimal value;
    private List<String> promotions;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public List<String> getPromotions() {
        return promotions;
    }
    
    public void setPromotions(List<String> promotions) {
        this.promotions = promotions;
    }
}