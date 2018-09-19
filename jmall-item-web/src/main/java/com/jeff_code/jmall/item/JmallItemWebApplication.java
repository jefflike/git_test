package com.jeff_code.jmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.jeff_code.jmall")
public class JmallItemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallItemWebApplication.class, args);
    }
}
