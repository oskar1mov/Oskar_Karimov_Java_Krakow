package org.example.service;

import org.example.strategy.CardPaymentStrategy;
import org.example.strategy.FullPointsPaymentStrategy;
import org.example.strategy.PartialPointsPaymentStrategy;
import org.example.strategy.PaymentStrategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.exception.NoValidPaymentOptionException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentOptimizerService {

    private final List<PaymentStrategy> strategies;

    public PaymentOptimizerService() {
        this.strategies = Arrays.asList(
            new CardPaymentStrategy(),
            new FullPointsPaymentStrategy(),
            new PartialPointsPaymentStrategy()
        );
    }

    public Map<String, BigDecimal> optimizePayments(List<Order> orders, List<PaymentMethod> methods) {
        Map<String, BigDecimal> result = new HashMap<>();
        Map<String, PaymentMethod> methodMap = new HashMap<>(
                methods.stream().collect(Collectors.toMap(PaymentMethod::getId, m -> m)));

        List<Order> sortedOrders = sortOrdersForGreedyAlgorithm(orders);

        for (Order order : sortedOrders) {
            processOrder(order, methodMap, result);
        }

        return result;
    }

    /**
     * This function is the core of our approach, which is a greedy algorithm to find the best payment method.
     * We process larger orders first to maximize total discount.
     * Orders are sorted by value in descending order.
     * 
     * @param List<Order> unsortedOrders the original list of orders
     * @return sorted list of orders by the order value descending
     */
    private List<Order> sortOrdersForGreedyAlgorithm(List<Order> unsortedOrders) {
        return unsortedOrders.stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .collect(Collectors.toList());
    }

    private void processOrder(Order order, Map<String, PaymentMethod> methodMap, Map<String, BigDecimal> result) {
        List<PaymentOption> paymentOptions = new ArrayList<>();
        
        for (PaymentStrategy strategy : strategies) {
            PaymentOption option = strategy.calculatePaymentOption(order, methodMap);
            if (option != null) {
                paymentOptions.add(option);
            }
        }
        
        paymentOptions.sort((o1, o2) -> {
            int discountComparison = o2.getTotalDiscount().compareTo(o1.getTotalDiscount());
            if (discountComparison != 0) return discountComparison;
            
            // If discounts are equal, prefer options that use more points
            BigDecimal o1Points = o1.getPayments().getOrDefault("PUNKTY", BigDecimal.ZERO);
            BigDecimal o2Points = o2.getPayments().getOrDefault("PUNKTY", BigDecimal.ZERO);
            return o2Points.compareTo(o1Points);
        });
        
        if (!paymentOptions.isEmpty()) {
            PaymentOption chosenOption = paymentOptions.getFirst();
            
            // Assume that there might be at most two payments (for the points + card solution)
            for (Map.Entry<String, BigDecimal> payment : chosenOption.getPayments().entrySet()) {
                String methodId = payment.getKey();
                BigDecimal amount = payment.getValue();
                
                PaymentMethod method = methodMap.get(methodId);
                method.setLimit(method.getLimit().subtract(amount));
                
                result.merge(methodId, amount, BigDecimal::add);
            }
        } else {
            throw new NoValidPaymentOptionException(order.getId());
        }
    }
    
    public static class PaymentOption {
        private Map<String, BigDecimal> payments = new HashMap<>();
        private BigDecimal totalDiscount = BigDecimal.ZERO;
        
        public void addPayment(String methodId, BigDecimal amount) {
            payments.put(methodId, amount);
        }
        
        public Map<String, BigDecimal> getPayments() {
            return payments;
        }
        
        public void setPayments(Map<String, BigDecimal> payments) {
            this.payments = payments;
        }
        
        public BigDecimal getTotalDiscount() {
            return totalDiscount;
        }
        
        public void setTotalDiscount(BigDecimal totalDiscount) {
            this.totalDiscount = totalDiscount;
        }
    }
}
