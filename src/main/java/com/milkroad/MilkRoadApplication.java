package com.milkroad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // habilita @Scheduled para tarefas automáticas
public class MilkRoadApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilkRoadApplication.class, args);
    }
}
