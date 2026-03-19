package com.ride.mate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RideMateBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideMateBackEndApplication.class, args);
    }

}
