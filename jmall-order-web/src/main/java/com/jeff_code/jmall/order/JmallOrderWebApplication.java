package com.jeff_code.jmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.jeff_code.jmall")
public class JmallOrderWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(JmallOrderWebApplication.class, args);
    }
}
