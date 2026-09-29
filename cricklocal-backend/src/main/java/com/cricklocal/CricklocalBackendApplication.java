package com.cricklocal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CricklocalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CricklocalBackendApplication.class, args);
    }
}