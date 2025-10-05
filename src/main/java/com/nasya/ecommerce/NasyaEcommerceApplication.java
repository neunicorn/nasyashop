package com.nasya.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = {
		"com.nasya.ecommerce", // Your main package
		"com.nasya.ecommerce.security" // Ensure the specific security package is included
})
public class NasyaEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NasyaEcommerceApplication.class, args);
	}

}
