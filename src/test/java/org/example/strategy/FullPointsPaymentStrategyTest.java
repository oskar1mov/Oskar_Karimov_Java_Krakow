package org.example.strategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService.PaymentOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FullPointsPaymentStrategyTest {

    private FullPointsPaymentStrategy strategy;
    private Map<String, PaymentMethod> methodMap;

    @BeforeEach
    void setUp() {
        strategy = new FullPointsPaymentStrategy();
        methodMap = new HashMap<>();

        PaymentMethod punkty = new PaymentMethod();
        punkty.setId("PUNKTY");
        punkty.setDiscount(15.0); // 15% discount for full points payment
        punkty.setLimit(new BigDecimal("120.00"));

        methodMap.put("PUNKTY", punkty);
    }

    @Test
    void shouldApplyFullPointsDiscountWhenEnoughPointsAvailable() {
        Order order = new Order();
        order.setId("ORDER1");
        order.setValue(new BigDecimal("100.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNotNull(result);
        assertEquals(new BigDecimal("15.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("85.00"), result.getPayments().get("PUNKTY"));
    }

    @Test
    void shouldReturnNullWhenPointsNotEnough() {
        Order order = new Order();
        order.setId("ORDER2");
        order.setValue(new BigDecimal("130.00")); // more than limit

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenPointsMethodMissing() {
        methodMap.remove("PUNKTY");

        Order order = new Order();
        order.setId("ORDER3");
        order.setValue(new BigDecimal("100.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNull(result);
    }

    @Test
    void shouldHandleExactLimitCase() {
        Order order = new Order();
        order.setId("ORDER4");
        order.setValue(new BigDecimal("120.00")); // =limit

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNotNull(result);
        assertEquals(new BigDecimal("18.00"), result.getTotalDiscount()); // 15%
        assertEquals(new BigDecimal("102.00"), result.getPayments().get("PUNKTY"));
    }

    @Test
    void shouldHandleZeroDiscount() {
        methodMap.get("PUNKTY").setDiscount(0.0);

        Order order = new Order();
        order.setId("ORDER5");
        order.setValue(new BigDecimal("100.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("100.00"), result.getPayments().get("PUNKTY"));
    }
}
