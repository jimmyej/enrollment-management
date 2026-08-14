package com.mitocode.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi studentsAPI() {
        return GroupedOpenApi.builder()
                .group("Students")
                .pathsToMatch("/api/v1/students/**")
                .addOpenApiCustomizer(getOpenApiCustomiser())
                .build();
    }
    @Bean
    public GroupedOpenApi coursesAPI() {
        return GroupedOpenApi.builder()
                .group("Courses")
                .pathsToMatch("/api/v1/courses/**")
                .addOpenApiCustomizer(getOpenApiCustomiser())
                .build();
    }

    @Bean
    public GroupedOpenApi enrollmentsAPI() {
        return GroupedOpenApi.builder()
                .group("Enrollments")
                .pathsToMatch("/api/v1/enrollments/**")
                .addOpenApiCustomizer(getOpenApiCustomiser())
                .build();
    }

    @Bean
    public GroupedOpenApi authAPI() {
        return GroupedOpenApi.builder()
                .group("Authentications")
                .pathsToMatch("/api/v1/auth/**")
                .addOpenApiCustomizer(getOpenApiCustomiser())
                .build();
    }

    public OpenApiCustomizer getOpenApiCustomiser() {

        return openAPI -> {
            // Define bearer security scheme
            SecurityScheme bearerScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT");
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }
            openAPI.getComponents().addSecuritySchemes("bearerAuth", bearerScheme);

            // Apply security requirement to all operations except auth endpoints
            openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                // Skip auth endpoints (sign-in / sign-up)
                if (path.startsWith("/api/v1/auth")) {
                    return;
                }
                operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                // keep userId header parameter for operations that require it
            }));
        };
    }
}
