package com.jeff_code.jmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.jeff_code.jmall")
@MapperScan(basePackages = "com.jeff_code.jmall.cart.mapper")
public class JmallCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallCartServiceApplication.class, args);
    }
}
