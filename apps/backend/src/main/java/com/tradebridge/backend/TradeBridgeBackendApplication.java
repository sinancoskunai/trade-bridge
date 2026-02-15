package com.tradebridge.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TradeBridgeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeBridgeBackendApplication.class, args);
	}

}
