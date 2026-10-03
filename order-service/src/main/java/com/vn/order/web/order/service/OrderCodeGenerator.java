package com.vn.order.web.order.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderCodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateOrderCode() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "ORD-" + datePart + "-" + randomPart;
    }
}
