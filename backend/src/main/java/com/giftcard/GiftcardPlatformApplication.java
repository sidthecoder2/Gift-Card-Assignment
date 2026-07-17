package com.giftcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GiftcardPlatformApplication {
    public static void main(String[] args) {
        // Windows JVMs sometimes report the default timezone using the old
        // alias "Asia/Calcutta" instead of the modern IANA name "Asia/Kolkata".
        // Postgres's tzdata doesn't recognize the old alias, so we pin it
        // explicitly before anything (including the JDBC driver) reads it.
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(GiftcardPlatformApplication.class, args);
    }
}