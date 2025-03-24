package com.gradingsystem.tesla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:.env")
public class TeslaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeslaApplication.class, args);
    }
}
