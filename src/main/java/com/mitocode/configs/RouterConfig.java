package com.mitocode.configs;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.mitocode.documents.Course;
import com.mitocode.documents.Enrollment;
import com.mitocode.documents.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mitocode.handlers.CourseHandler;
import com.mitocode.handlers.EnrollmentHandler;
import com.mitocode.handlers.StudentHandler;

@Configuration
public class RouterConfig {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/students",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = StudentHandler.class,
                    beanMethod = "findAll",
                    operation = @Operation(
                        operationId = "findAll",
                        responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "successful operation",
                                    content = @Content(schema = @Schema(
                                            implementation = Student.class
                                    ))
                            )
                        }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/students/pages",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = StudentHandler.class,
                    beanMethod = "findPage",
                    operation = @Operation(
                            operationId = "findPage",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Student.class
                                            ))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/students/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = StudentHandler.class,
                    beanMethod = "findById",
                    operation = @Operation(
                        operationId = "findById",
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "successful operation",
                                        content = @Content(schema = @Schema(
                                                implementation = Student.class
                                        ))
                                ),
                                @ApiResponse(responseCode = "404",description = "Student not found with given id")
                        },
                        parameters = {
                                @Parameter(in = ParameterIn.PATH,name = "id")
                        }

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/students",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = StudentHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "create",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Student.class
                                            ))
                                    )
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Student.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/students/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.PUT,
                    beanClass = StudentHandler.class,
                    beanMethod = "update",
                    operation = @Operation(
                            operationId = "update",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Student.class
                                            ))
                                    )
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Student.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/students/{id}/upload",
                    produces = {
                            MediaType.ALL_VALUE
                    },
                    consumes = {
                            MediaType.ALL_VALUE
                    },
                    method = RequestMethod.PUT,
                    beanClass = StudentHandler.class,
                    beanMethod = "upload",
                    operation = @Operation(
                            operationId = "upload",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Student.class
                                            ))
                                    )
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Student.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/students/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.DELETE,
                    beanClass = StudentHandler.class,
                    beanMethod = "delete",
                    operation = @Operation(
                            operationId = "delete",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Student.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404",description = "Student not found with given id")
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            }

                    )
            )
    })
    public RouterFunction<ServerResponse> studentRoutes(StudentHandler handler) {
        return route(GET("/api/v1/students"), handler::findAll)
                .andRoute(GET("/api/v1/students/pages"), handler::findPage)
                .andRoute(GET("/api/v1/students/{id}"), handler::findById)
                .andRoute(POST("/api/v1/students"), handler::create)
                .andRoute(PUT("/api/v1/students/{id}"), handler::update)
                .andRoute(PUT("/api/v1/students/{id}/upload").and(RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA)), handler::upload)
                .andRoute(DELETE("/api/v1/students/{id}"), handler::delete);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/courses",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = CourseHandler.class,
                    beanMethod = "findAll",
                    operation = @Operation(
                            operationId = "findAll",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/courses/pages",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = CourseHandler.class,
                    beanMethod = "findPage",
                    operation = @Operation(
                            operationId = "findPage",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/courses/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = CourseHandler.class,
                    beanMethod = "findById",
                    operation = @Operation(
                            operationId = "findById",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404",description = "Course not found with given id")
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            }

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/courses",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = CourseHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "create",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Course.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/courses/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.PUT,
                    beanClass = CourseHandler.class,
                    beanMethod = "update",
                    operation = @Operation(
                            operationId = "update",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    )
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Course.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/courses/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.DELETE,
                    beanClass = CourseHandler.class,
                    beanMethod = "delete",
                    operation = @Operation(
                            operationId = "delete",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404",description = "Course not found with given id")
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            }

                    )
            )
    })
    public RouterFunction<ServerResponse> courseRoutes(CourseHandler handler) {
        return route(GET("/api/v1/courses"), handler::findAll)
                .andRoute(GET("/api/v1/courses/pages"), handler::findPage)
                .andRoute(GET("/api/v1/courses/{id}"), handler::findById)
                .andRoute(POST("/api/v1/courses"), handler::create)
                .andRoute(PUT("/api/v1/courses/{id}"), handler::update)
                .andRoute(DELETE("/api/v1/courses/{id}"), handler::delete);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/enrollments",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = EnrollmentHandler.class,
                    beanMethod = "findAll",
                    operation = @Operation(
                            operationId = "findAll",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Enrollment.class
                                            ))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/enrollments/pages",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = EnrollmentHandler.class,
                    beanMethod = "findPage",
                    operation = @Operation(
                            operationId = "findPage",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Course.class
                                            ))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/enrollments/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = EnrollmentHandler.class,
                    beanMethod = "findById",
                    operation = @Operation(
                            operationId = "findById",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Enrollment.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404",description = "Enrollment not found with given id")
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            }

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/enrollments",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = EnrollmentHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "create",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Enrollment.class
                                            ))
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Enrollment.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/enrollments/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.PUT,
                    beanClass = EnrollmentHandler.class,
                    beanMethod = "update",
                    operation = @Operation(
                            operationId = "update",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Enrollment.class
                                            ))
                                    )
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(
                                            implementation = Enrollment.class
                                    ))
                            )

                    )
            ),
            @RouterOperation(
                    path = "/api/v1/enrollments/{id}",
                    produces = {
                            MediaType.APPLICATION_JSON_VALUE
                    },
                    method = RequestMethod.DELETE,
                    beanClass = EnrollmentHandler.class,
                    beanMethod = "delete",
                    operation = @Operation(
                            operationId = "delete",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = Enrollment.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404",description = "Enrollment not found with given id")
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH,name = "id")
                            }

                    )
            )
    })
    public RouterFunction<ServerResponse> enrollmentRoutes(EnrollmentHandler handler) {
        return route(GET("/api/v1/enrollments"), handler::findAll)
                .andRoute(GET("/api/v1/enrollments/pages"), handler::findPage)
                .andRoute(GET("/api/v1/enrollments/{id}"), handler::findById)
                .andRoute(POST("/api/v1/enrollments"), handler::create)
                .andRoute(PUT("/api/v1/enrollments/{id}"), handler::update)
                .andRoute(DELETE("/api/v1/enrollments/{id}"), handler::delete);
    }
	
}
