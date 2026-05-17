package com.abanoj.scheman.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.admin")
@Getter
@Setter
public class AdminProperties {
    private String email;
    private String password;
}
