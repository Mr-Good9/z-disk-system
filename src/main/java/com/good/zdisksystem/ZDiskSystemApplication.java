package com.good.zdisksystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@MapperScan(basePackages = "com.good.zdisksystem.mapper")
@SpringBootApplication
@EnableWebSocketMessageBroker
public class ZDiskSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZDiskSystemApplication.class, args);
    }

}
