package com.jeff_code.jmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.jeff_code.jmall.manage.mapper")
@ComponentScan("com.jeff_code.jmall")
public class JmallManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallManageServiceApplication.class, args);
    }
}
