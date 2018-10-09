package com.jeff_code.jmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.jeff_code.jmall")
@MapperScan(basePackages = "com.jeff_code.jmall.payment.mapper")
public class JmallPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallPaymentApplication.class, args);
    }
}
