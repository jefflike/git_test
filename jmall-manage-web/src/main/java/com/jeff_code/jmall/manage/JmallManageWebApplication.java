package com.jeff_code.jmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.jeff_code.jmall")
public class JmallManageWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallManageWebApplication.class, args);
    }
}
