package com.vn.shipment.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class ShipmentCodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateShipmentCode() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "SHP-" + datePart + "-" + randomPart;
    }

    public static String generateTrackingNumber(String carrierPrefix) {
        String prefix = carrierPrefix != null ? carrierPrefix.toUpperCase() : "EXP";
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        int randomPart = ThreadLocalRandom.current().nextInt(1000000, 9999999);
        return prefix + datePart + randomPart;
    }
}
