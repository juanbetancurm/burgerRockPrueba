package com.rockburger.arquetipo2024;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.rockburger.arquetipo2024.adapters.driven.feign")
public class Arquetipo2024Application {
	public static void main(String[] args) {
		SpringApplication.run(Arquetipo2024Application.class, args);
	}

}
