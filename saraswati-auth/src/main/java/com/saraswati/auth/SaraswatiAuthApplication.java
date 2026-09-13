package com.saraswati.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SaraswatiAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaraswatiAuthApplication.class, args);
    }
}
