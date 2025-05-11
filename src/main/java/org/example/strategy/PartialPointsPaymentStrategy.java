package org.example.strategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService.PaymentOption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Strategy for payment using a combination of points and another payment method
 */
public class PartialPointsPaymentStrategy implements PaymentStrategy {

    private static final String POINTS = "PUNKTY";

    @Override
    public PaymentOption calculatePaymentOption(Order order, Map<String, PaymentMethod> methodMap) {
        PaymentMethod points = methodMap.get(POINTS);
        BigDecimal orderValue = order.getValue();
        
        if (points == null || points.getLimit().equals(BigDecimal.ZERO)) return null;
        BigDecimal pointsAvailable = points.getLimit();
        
        // We're assuming points will never fully cover the order value,
        // so we don't need to check if pointsAvailable >= orderValue
        if ( points.getLimit().compareTo(orderValue) >= 0 ) return null;

        BigDecimal discountPercent = findDiscountPercent(points, orderValue, pointsAvailable);
        BigDecimal discountAmount = orderValue.multiply(discountPercent).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedTotal = orderValue.subtract(discountAmount);
        
        BigDecimal pointsPayment = pointsAvailable.min(discountedTotal);
        BigDecimal remainingToPay = discountedTotal.subtract(pointsPayment).setScale(2, RoundingMode.HALF_UP);
        
        for (PaymentMethod method : methodMap.values()) {
            if (!method.getId().equals(POINTS) && method.getLimit().compareTo(remainingToPay) >= 0) {
                PaymentOption option = new PaymentOption();
                option.addPayment(POINTS, pointsPayment);
                option.addPayment(method.getId(), remainingToPay);
                option.setTotalDiscount(discountAmount);
                return option;
            }
        }
        
        return null;
    }

    /**
     * Find the discount percent based on points available
     */
    private BigDecimal findDiscountPercent(PaymentMethod points, BigDecimal orderValue, 
                                          BigDecimal pointsAvailable) {
        BigDecimal tenPercentOfOrder = orderValue.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
        if (pointsAvailable.compareTo(tenPercentOfOrder) >= 0) {
            return BigDecimal.valueOf(0.1);
        } else {
            return BigDecimal.ZERO;
        }
    }
} 