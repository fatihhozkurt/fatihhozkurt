package com.fatihozkurt.fatihozkurtcom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application entry point.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class FatihozkurtcomApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args runtime arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(FatihozkurtcomApplication.class, args);
	}

}
