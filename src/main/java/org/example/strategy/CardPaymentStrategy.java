package org.example.strategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService.PaymentOption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Strategy for payment using promotional cards
 */
public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentOption calculatePaymentOption(Order order, Map<String, PaymentMethod> methodMap) {
        if (order.getPromotions() == null || order.getPromotions().isEmpty()) {
            return null;
        }
        
        PaymentOption bestOption = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;
        
        for (String promoId : order.getPromotions()) {
            PaymentMethod method = methodMap.get(promoId);
            if (method == null) continue;
            
            if (method.getLimit().compareTo(order.getValue()) >= 0) {
                BigDecimal discountAmount = order.getValue()
                        .multiply(new BigDecimal(method.getDiscount()).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                
                BigDecimal finalAmount = order.getValue().subtract(discountAmount);
                
                if (discountAmount.compareTo(bestDiscount) > 0) {
                    bestDiscount = discountAmount;
                    bestOption = new PaymentOption();
                    bestOption.addPayment(method.getId(), finalAmount);
                    bestOption.setTotalDiscount(discountAmount);
                }
            }
        }
        
        return bestOption;
    }
} 