package com.abanoj.scheman.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secretKey;
    private long expiration;
    private Refresh refresh = new Refresh();

    @Getter
    @Setter
    public static class Refresh {
        private long expiration;
    }
}
