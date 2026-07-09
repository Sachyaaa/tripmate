package com.tripmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TripMateApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripMateApplication.class, args);
    }
}
