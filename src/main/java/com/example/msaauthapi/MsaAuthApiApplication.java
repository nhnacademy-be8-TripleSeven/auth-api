package com.example.msaauthapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsaAuthApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsaAuthApiApplication.class, args);
    }

}
