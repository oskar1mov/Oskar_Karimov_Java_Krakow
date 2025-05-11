package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.example.dto.Order;
import org.example.dto.PaymentMethod;
import org.example.service.PaymentOptimizerService;

public class Main {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        List<Order> orders = mapper.readValue(new File("orders.json"), new TypeReference<>() {});
        List<PaymentMethod> methods = mapper.readValue(new File("paymentmethods.json"), new TypeReference<>() {});

        PaymentOptimizerService optimizer = new PaymentOptimizerService();
        Map<String, BigDecimal> result = optimizer.optimizePayments(orders, methods);

        result.forEach((method, value) -> System.out.println(method + " " + value.setScale(2, RoundingMode.HALF_UP)));
    }
}
