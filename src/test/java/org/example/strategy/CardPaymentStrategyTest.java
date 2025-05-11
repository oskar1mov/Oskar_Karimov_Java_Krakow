package org.example.strategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService.PaymentOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CardPaymentStrategyTest {

    private CardPaymentStrategy strategy;
    private Map<String, PaymentMethod> methodMap;

    @BeforeEach
    void setUp() {
        strategy = new CardPaymentStrategy();
        methodMap = new HashMap<>();
        
        // Set up payment methods based on paymentmethods.json
        PaymentMethod punkty = new PaymentMethod();
        punkty.setId("PUNKTY");
        punkty.setDiscount(0.0);
        punkty.setLimit(new BigDecimal("100.00"));
        
        PaymentMethod mZysk = new PaymentMethod();
        mZysk.setId("mZysk");
        mZysk.setDiscount(10.0);
        mZysk.setLimit(new BigDecimal("180.00"));
        
        PaymentMethod bosBankrut = new PaymentMethod();
        bosBankrut.setId("BosBankrut");
        bosBankrut.setDiscount(5.0);
        bosBankrut.setLimit(new BigDecimal("200.00"));
        
        methodMap.put("PUNKTY", punkty);
        methodMap.put("mZysk", mZysk);
        methodMap.put("BosBankrut", bosBankrut);
    }

    @Test
    void shouldReturnCorrectDiscountForSinglePromotion() {
        // Based on ORDER1 from orders.json
        Order order = new Order();
        order.setId("ORDER1");
        order.setValue(new BigDecimal("100.00"));
        order.setPromotions(List.of("mZysk"));
        
        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("90.00"), result.getPayments().get("mZysk"));
    }
    
    @Test
    void shouldChooseBestDiscountWhenMultiplePromotionsAvailable() {
        // Based on ORDER3 from orders.json
        Order order = new Order();
        order.setId("ORDER3");
        order.setValue(new BigDecimal("150.00"));
        order.setPromotions(List.of("mZysk", "BosBankrut"));
        
        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("15.00"), result.getTotalDiscount()); // 10% of 150
        assertEquals(new BigDecimal("135.00"), result.getPayments().get("mZysk"));
        assertNull(result.getPayments().get("BosBankrut")); // Not used as mZysk gives better discount
    }
    
    @Test
    void shouldRespectPaymentMethodLimit() {
        // Order value exceeds mZysk limit
        Order order = new Order();
        order.setId("TEST_EXCEED_LIMIT");
        order.setValue(new BigDecimal("190.00")); // Exceeds mZysk limit of 180
        order.setPromotions(List.of("mZysk", "BosBankrut"));
        
        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("9.50"), result.getTotalDiscount()); // 5% of 190
        assertEquals(new BigDecimal("180.50"), result.getPayments().get("BosBankrut"));
    }
    
    @Test
    void shouldReturnNullWhenNoPromotionsProvided() {
        // Based on ORDER4 from orders.json
        Order order = new Order();
        order.setId("ORDER4");
        order.setValue(new BigDecimal("50.00"));
        order.setPromotions(null);
        
        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);
        
        assertNull(result);
    }
    
    @Test
    void shouldReturnNullWhenPromotionsListEmpty() {
        Order order = new Order();
        order.setId("TEST_EMPTY_PROMO");
        order.setValue(new BigDecimal("50.00"));
        order.setPromotions(List.of());
        
        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);
        
        assertNull(result);
    }
    
    @Test
    void shouldHandleNonExistentPromotionId() {
        Order order = new Order();
        order.setId("TEST_NONEXISTENT_PROMO");
        order.setValue(new BigDecimal("100.00"));
        order.setPromotions(List.of("NON_EXISTENT_PROMO"));
        
        PaymentOption result = strategy.calculatePaymentOption(order, methodMap);
        
        assertNull(result);
    }
}