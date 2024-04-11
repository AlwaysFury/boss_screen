package com.boss.bossscreen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.boss.bossscreen")
@SpringBootApplication
@EnableScheduling
public class BossScreenApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossScreenApplication.class, args);
    }

}
