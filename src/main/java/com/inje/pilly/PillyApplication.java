package com.inje.pilly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching

public class PillyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PillyApplication.class, args);
	}

}
