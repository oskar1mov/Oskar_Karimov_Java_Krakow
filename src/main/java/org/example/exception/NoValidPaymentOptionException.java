package org.example.exception;

/**
 * Exception thrown when no valid payment option can be found for an order.
 */
public class NoValidPaymentOptionException extends RuntimeException {
    public NoValidPaymentOptionException(String orderId) {
        super("No valid payment option found for order: " + orderId);
    }
} 