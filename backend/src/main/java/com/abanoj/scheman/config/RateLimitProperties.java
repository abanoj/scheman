package com.abanoj.scheman.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.rate-limit")
@Getter
@Setter
public class RateLimitProperties {
    private int maxAttempts;
    private int refillMinutes;
}
