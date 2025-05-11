package org.example.strategy;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService.PaymentOption;

import java.util.Map;

/**
 * Strategy interface for different payment calculation approaches
 */
public interface PaymentStrategy {
    PaymentOption calculatePaymentOption(Order order, Map<String, PaymentMethod> methodMap);
} 