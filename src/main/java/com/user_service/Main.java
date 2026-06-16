package com.user_service;


import com.user_service.config.HibernateConfig;
import com.user_service.console.ConsoleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Приложение user-service успешно запущено");

        try {

            if (HibernateConfig.getSessionFactory() == null) {
                logger.error("Не произошла инициализация SessionFactory");
                return;
            }

            logger.info("Инициализация SessionFactory прошла успешно");

            ConsoleInterface consoleInterface = new ConsoleInterface();
            consoleInterface.start();
        } catch (Exception e) {
            logger.error("Возникла ошибка при работе приложения", e);
            throw new RuntimeException("Произошла ошибка: " + e.getMessage());
        } finally {
            logger.info("Приложение user-service успешно завершено");

        }
    }
}