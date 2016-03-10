package com.waitingforcode.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServletContainerConfigurationRunner {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Object[]{ServletContainerConfigurationRunner.class}, args);
    }

}
