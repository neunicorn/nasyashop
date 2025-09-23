package com.nasya.ecommerce.service;


import java.util.function.Supplier;

public interface RateLimittingSevice {
    <T> T executeWithRateimit(String key, Supplier<T> operation);
}
