package com.ride.mate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.ride.mate.config.PayHereConfig;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PayHereConfig.class)
public class RideMateBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideMateBackEndApplication.class, args);
    }

}
