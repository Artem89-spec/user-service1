package com.user_service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsoleRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleRunner.class);
    private final ConsoleInterface consoleInterface;

    @Autowired
    public ConsoleRunner(ConsoleInterface consoleInterface) {
        this.consoleInterface = consoleInterface;
    }

    public void run() {
        logger.info("Запуск консольного интерфейса");
        consoleInterface.start();
    }
}
