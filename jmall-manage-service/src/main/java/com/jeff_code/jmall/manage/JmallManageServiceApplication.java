package com.jeff_code.jmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.jeff_code.jmall.manage.mapper")
public class JmallManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallManageServiceApplication.class, args);
    }
}
