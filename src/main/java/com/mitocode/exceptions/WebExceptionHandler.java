package com.mitocode.exceptions;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(-1)
public class WebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public WebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resourceProperties,
                               ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resourceProperties, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> generalError = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Map<String, Object> customError = new HashMap<>();

        String message = "message";
        String statusStr = "status";

        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        String statusCode = String.valueOf(generalError.get(statusStr));


        switch (statusCode) {
            case "400":
                customError.put(message, "Bad request");
                customError.put(statusStr, "400");
                status = HttpStatus.BAD_REQUEST;
                break;
            case "404":
                status = HttpStatus.NOT_FOUND;
                break;
            case "500":
                customError.put(message, "Internal server error");
                customError.put(statusStr, "500");
                break;
            default:
                customError.put(message, "Unknown error");
                customError.put(statusStr, "999");
                status = HttpStatus.SEE_OTHER;
                break;
        }

        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(customError));
    }
}
