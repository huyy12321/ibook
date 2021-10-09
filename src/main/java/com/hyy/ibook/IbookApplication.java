package com.hyy.ibook;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.hyy.ibook.mapper")
public class IbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(IbookApplication.class, args);
    }

}
