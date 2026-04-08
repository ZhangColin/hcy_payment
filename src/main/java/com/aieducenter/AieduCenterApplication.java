package com.aieducenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 海创元智研云平台主启动类
 *
 * <p>通过 Composite Build 引用本地 cartisan-boot 框架
 */
@SpringBootApplication
public class AieduCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AieduCenterApplication.class, args);
    }
}
