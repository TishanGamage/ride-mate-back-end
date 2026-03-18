package com.ride.mate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PayHere Configuration
 * Holds PayHere payment gateway credentials and API endpoint configuration
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payhere")
public class PayHereConfig {

    private String merchantId;
    private String apiBaseUrl;
    private Api api = new Api();

    @Getter
    @Setter
    public static class Api {
        private String chargePath = "/pay/charge";
    }
}

