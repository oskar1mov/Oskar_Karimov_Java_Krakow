package org.example.service;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.exception.NoValidPaymentOptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentOptimizerServiceTest {

    private PaymentOptimizerService service;

    @BeforeEach
    void setUp() {
        service = new PaymentOptimizerService();
    }

    @Test
    void testOptimizePayments_WithSingleOrder_SingleMethod() {
        // Given
        Order order = createOrder("order1", "100.00", Arrays.asList("CARD1"));
        PaymentMethod card = createPaymentMethod("CARD1", 10.0, "200.00");
        
        // When
        Map<String, BigDecimal> result = service.optimizePayments(
            Collections.singletonList(order),
            Collections.singletonList(card)
        );
        
        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey("CARD1"));
        assertEquals(new BigDecimal("90.00"), result.get("CARD1")); // 100 - 10% discount
    }
    
    @Test
    void testOptimizePayments_PreferHigherDiscountCard() {
        // Given
        Order order = createOrder("order1", "100.00", Arrays.asList("CARD1", "CARD2"));
        PaymentMethod card1 = createPaymentMethod("CARD1", 10.0, "200.00");
        PaymentMethod card2 = createPaymentMethod("CARD2", 15.0, "200.00");
        
        // When
        Map<String, BigDecimal> result = service.optimizePayments(
            Collections.singletonList(order),
            Arrays.asList(card1, card2)
        );
        
        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey("CARD2"));
        assertEquals(new BigDecimal("85.00"), result.get("CARD2")); // 100 - 15% discount
    }

    @Test
    void testOptimizePayments_MultipleOrders_ProcessLargerFirst() {
        // Given
        Order order1 = createOrder("order1", "50.00", Arrays.asList("CARD1"));
        Order order2 = createOrder("order2", "150.00", Arrays.asList("CARD1"));
        PaymentMethod card = createPaymentMethod("CARD1", 10.0, "200.00");
        
        // When
        Map<String, BigDecimal> result = service.optimizePayments(
            Arrays.asList(order1, order2), // Note the order - service should sort by value
            Collections.singletonList(card)
        );
        
        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey("CARD1"));
        // Should process larger order first: 150 - 15 = 135, then 50 - 5 = 45, total 180
        assertEquals(new BigDecimal("180.00"), result.get("CARD1"));
    }
    
    @Test
    void testOptimizePayments_InsufficientLimits() {
        // Given
        Order order = createOrder("order1", "100.00", Arrays.asList("CARD1"));
        PaymentMethod card = createPaymentMethod("CARD1", 10.0, "50.00"); // Not enough limit
        
        // Then
        assertThrows(NoValidPaymentOptionException.class, () -> {
            service.optimizePayments(
                Collections.singletonList(order),
                Collections.singletonList(card)
            );
        });
    }
    
    @Test
    void testOptimizePayments_LimitsAreUpdated() {
        // Given
        Order order1 = createOrder("order1", "40.00", Arrays.asList("CARD1"));
        Order order2 = createOrder("order2", "30.00", Arrays.asList("CARD1"));
        PaymentMethod card = createPaymentMethod("CARD1", 10.0, "100.00");
        
        // When
        Map<String, BigDecimal> result = service.optimizePayments(
            Arrays.asList(order1, order2),
            Collections.singletonList(card)
        );
        
        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey("CARD1"));
        
        // First order: 40 - 10% = 36, limit reduced to 64
        // Second order: 30 - 10% = 27, total payment = 63
        assertEquals(new BigDecimal("63.00"), result.get("CARD1"));
        
        // Verify the card limit was updated
        assertEquals(new BigDecimal("37.00"), card.getLimit()); // 100 - 36 - 27 = 37
    }
    
    @Test
    void testOptimizePayments_NoValidPaymentOption() {
        // Given
        Order order = createOrder("order1", "100.00", Arrays.asList()); // No promotions
        PaymentMethod card = createPaymentMethod("CARD1", 10.0, "200.00");
        
        // Then
        assertThrows(NoValidPaymentOptionException.class, () -> {
            service.optimizePayments(
                Collections.singletonList(order),
                Collections.singletonList(card)
            );
        });
    }
    
    // Helper methods to create test objects
    private Order createOrder(String id, String value, List<String> promotions) {
        Order order = new Order();
        order.setId(id);
        order.setValue(new BigDecimal(value));
        order.setPromotions(promotions);
        return order;
    }
    
    private PaymentMethod createPaymentMethod(String id, double discount, String limit) {
        PaymentMethod method = new PaymentMethod();
        method.setId(id);
        method.setDiscount(discount);
        method.setLimit(new BigDecimal(limit));
        return method;
    }
}
