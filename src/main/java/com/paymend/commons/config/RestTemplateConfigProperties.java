package com.paymend.commons.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "paymend.http-client")
@Data
public class RestTemplateConfigProperties {

    private Duration connectTimeout = Duration.ofSeconds(30);
    private Duration readTimeout = Duration.ofSeconds(60);
    private Retry retry = new Retry();
    private Correlation correlation = new Correlation();

    @Data
    public static class Retry {
        private int maxAttempts = 3;
        private Duration initialInterval = Duration.ofSeconds(1);
        private Duration maxInterval = Duration.ofSeconds(10);
        private double multiplier = 2.0;
        private List<Integer> retryableStatusCodes = List.of(500, 502, 503, 504);
    }

    @Data
    public static class Correlation {
        private boolean enabled = true;
        private String requestIdHeader = "request-id";
        private String requestIdPrefix = "REQ-";
        private int requestIdLength = 12;
    }
}