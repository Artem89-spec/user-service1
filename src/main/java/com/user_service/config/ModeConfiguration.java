package com.user_service.config;

import com.user_service.console.ConsoleRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ModeConfiguration.class);

    @Bean
    @ConditionalOnProperty(name = "app.mode", havingValue = "console")
    public ApplicationRunner consoleRunner(ConsoleRunner consoleRunner) {
        return args -> {
            logger.info("Запуск приложения в консольном режиме");
            consoleRunner.run();
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.mode", havingValue = "http", matchIfMissing = true)
    public ApplicationRunner httpRunner() {
        return args -> {
            logger.info("Запуск приложения в HTTP режиме (REST API)");
        };
    }
}

