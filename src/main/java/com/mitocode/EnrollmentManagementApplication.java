package com.mitocode;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Enrollment management service", version = "1.0", description = "Documentation APIs v1.0"))
public class EnrollmentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnrollmentManagementApplication.class, args);
	}

}
