package org.example.strategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService.PaymentOption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Strategy for payment using only points when they can cover the full amount
 */
public class FullPointsPaymentStrategy implements PaymentStrategy {

    private static final String POINTS = "PUNKTY";
    
    @Override
    public PaymentOption calculatePaymentOption(Order order, Map<String, PaymentMethod> methodMap) {
        PaymentMethod punkty = methodMap.get(POINTS);
        if (punkty == null || punkty.getLimit().compareTo(order.getValue()) < 0) {
            return null;
        }
        
        BigDecimal discountAmount = order.getValue()
                .multiply(new BigDecimal(punkty.getDiscount()).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal finalAmount = order.getValue().subtract(discountAmount);
        
        PaymentOption option = new PaymentOption();
        option.addPayment(POINTS, finalAmount);
        option.setTotalDiscount(discountAmount);
        
        return option;
    }
} 