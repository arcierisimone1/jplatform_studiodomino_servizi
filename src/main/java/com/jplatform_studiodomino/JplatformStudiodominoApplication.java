package com.jplatform_studiodomino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JplatformStudiodominoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JplatformStudiodominoApplication.class, args);
	}

}
