package com.nasya.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NasyaEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NasyaEcommerceApplication.class, args);
	}

}
