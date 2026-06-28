package com.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
public class UserServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceApplication.class);

    public static void main(String[] args) {
        logger.info("Запуск приложения user-service");
        SpringApplication.run(UserServiceApplication.class, args);
        logger.info("Приложение user-service успешно запущено");
    }
}