package com.rewritesolutions.ai.spring_ai_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring AI Application.
 * This class bootstraps the Spring Boot application with auto-configuration.
 *
 * <p>The {@link SpringBootApplication} annotation is a convenience annotation that combines:
 * <ul>
 *   <li>{@code @Configuration} - marks this class as a source of bean definitions</li>
 *   <li>{@code @EnableAutoConfiguration} - enables Spring Boot's auto-configuration</li>
 *   <li>{@code @ComponentScan} - enables component scanning in the package and sub-packages</li>
 * </ul>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class SpringAiAppApplication {

	/**
	 * Application entry point that starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringAiAppApplication.class, args);
	}

}
