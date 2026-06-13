package com.user_service.config;

import com.user_service.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HibernateConfig {

    private static SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(HibernateConfig.class);

    static {
        try {
            sessionFactory = createSessionFactory();
            logger.info("Hibernate успешно инициализировал SessionFactory");
        } catch (Exception e) {
            logger.error("Ошибка инициализации Hibernate SessionFactory", e);
            throw new ExceptionInInitializerError("Не удалось инициализировать Hibernate: " + e.getMessage());
        }
    }

    private static SessionFactory createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            Properties properties = new Properties();
            InputStream inputStream = HibernateConfig.class
                    .getClassLoader()
                    .getResourceAsStream("hibernate.properties");

            if (inputStream == null) {
                throw new RuntimeException("файл hibernate.properties не найден");
            }

            properties.load(inputStream);
            configuration.setProperties(properties);
            configuration.addAnnotatedClass(User.class);

            return configuration.buildSessionFactory();

        } catch (IOException e) {
            logger.error("Ошибка загрузки hibernate.properties", e);
            throw new RuntimeException("Ошибка загрузки настроек Hibernate", e);
        } catch (RuntimeException e) {
            logger.error("Ошибка построения SessionFactory", e);
            throw new RuntimeException("Hibernate не удалось построить SessionFactory", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Hibernate успешно закрыл SessionFactory");
        }
    }
}
