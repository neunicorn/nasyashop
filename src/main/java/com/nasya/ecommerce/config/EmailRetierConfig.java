package com.nasya.ecommerce.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class EmailRetierConfig {

    @Value("${email.retier.max-attemps}")
    private Integer maxAttempts;
    @Value("${email.retier.duration}")
    private Duration duration;

    @Bean
    public Retry emailRetier(){
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(duration)
                .retryExceptions(IOException.class)
                .build();

        return Retry.of("emailRetier", config);
    }
}
