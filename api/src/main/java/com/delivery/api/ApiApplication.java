package com.delivery.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;



@SpringBootApplication(scanBasePackages = {
        "com.delivery.api",
        "com.delivery.application"
})
public class ApiApplication {
    public static void main(String[] args){
        SpringApplication.run(ApiApplication.class, args);
    }
}
