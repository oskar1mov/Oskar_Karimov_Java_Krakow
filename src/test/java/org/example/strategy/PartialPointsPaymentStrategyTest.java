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

class PartialPointsPaymentStrategyTest {

    private PartialPointsPaymentStrategy strategy;
    private Map<String, PaymentMethod> methodMap;

    @BeforeEach
    void setUp() {
        strategy = new PartialPointsPaymentStrategy();
        methodMap = new HashMap<>();

        PaymentMethod punkty = new PaymentMethod();
        punkty.setId("PUNKTY");
        punkty.setDiscount(15.0); // Ignored in this strategy
        punkty.setLimit(new BigDecimal("20.00")); // < order value

        PaymentMethod mZysk = new PaymentMethod();
        mZysk.setId("mZysk");
        mZysk.setDiscount(10.0); // Ignored here
        mZysk.setLimit(new BigDecimal("200.00"));

        methodMap.put("PUNKTY", punkty);
        methodMap.put("mZysk", mZysk);
    }

    @Test
    void shouldApplyPartialPointsDiscountWhen10PercentCovered() {
        Order order = new Order();
        order.setId("ORDER1");
        order.setValue(new BigDecimal("100.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getTotalDiscount()); // 10% of 100
        assertEquals(new BigDecimal("20.00"), result.getPayments().get("PUNKTY")); // points used
        assertEquals(new BigDecimal("70.00"), result.getPayments().get("mZysk"));  // rest after discount and points
    }

    @Test
    void shouldReturnNullWhenPointsMethodMissing() {
        methodMap.remove("PUNKTY");

        Order order = new Order();
        order.setId("ORDER2");
        order.setValue(new BigDecimal("100.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenNoOtherPaymentMethodCanCoverRemaining() {
        methodMap.get("mZysk").setLimit(new BigDecimal("10.00")); // too low for remaining

        Order order = new Order();
        order.setId("ORDER4");
        order.setValue(new BigDecimal("100.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenPointsCanFullyCoverOrder() {
        methodMap.get("PUNKTY").setLimit(new BigDecimal("100.00")); // enough for full payment

        Order order = new Order();
        order.setId("ORDER5");
        order.setValue(new BigDecimal("80.00"));

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNull(result);
    }

    @Test
    void shouldUseExactPointsAndMethodMatch() {
        methodMap.get("PUNKTY").setLimit(new BigDecimal("10.00"));
        methodMap.get("mZysk").setLimit(new BigDecimal("81.00")); // exactly enough

        Order order = new Order();
        order.setId("ORDER6");
        order.setValue(new BigDecimal("90.00")); // 10% discount = 9.00 => pay 81.00

        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);

        assertNotNull(result);
        assertEquals(new BigDecimal("9.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("10.00"), result.getPayments().get("PUNKTY"));
        assertEquals(new BigDecimal("71.00"), result.getPayments().get("mZysk"));
    }
}
