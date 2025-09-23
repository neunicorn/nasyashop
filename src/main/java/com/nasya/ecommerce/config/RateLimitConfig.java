package com.nasya.ecommerce.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${rate.limit.default}")
    private int defaultLimitForPeriod;
    @Value("${rate.limit.period}")
    private int limitRefreshPeriodInSeconds;
    @Value("${rate.limit.timeout}")
    private int timeoutInSeconds;

    @Bean
    public RateLimiterConfig rateLimiterConfig(){
        return RateLimiterConfig.custom()
                .limitForPeriod(defaultLimitForPeriod)
                .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriodInSeconds))
                .timeoutDuration(Duration.ofSeconds(timeoutInSeconds))
                .build();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig rateLimiterConfig){
        return RateLimiterRegistry.of(rateLimiterConfig);
    }
}
