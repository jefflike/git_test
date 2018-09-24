package com.jeff_code.jmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.jeff_code.jmall")
public class JmallListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallListServiceApplication.class, args);
    }
}
