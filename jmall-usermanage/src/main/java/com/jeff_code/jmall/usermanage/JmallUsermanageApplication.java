package com.jeff_code.jmall.usermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.jeff_code.jmall.usermanage.mapper")
public class JmallUsermanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmallUsermanageApplication.class, args);
    }
}
