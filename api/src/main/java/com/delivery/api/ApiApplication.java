package com.delivery.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication(scanBasePackages = {//이거의 역할은 그냥 api모듈스캔해서 되게끔하겠다 이거같은데
        "com.delivery.api"
})
public class ApiApplication {
    public static void main(String[] args){
        SpringApplication.run(ApiApplication.class, args);
    }
}
