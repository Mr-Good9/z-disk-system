package com.good.zdisksystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.good.zdisksystem.mapper")
@SpringBootApplication
public class ZDiskSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZDiskSystemApplication.class, args);
    }

}
