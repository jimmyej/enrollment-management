package com.mitocode.validators;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@Component
public class RequestValidator {

    private final Validator validator;

	@Autowired
	public RequestValidator(Validator validator){
		this.validator = validator;
	}
	
	public <T> Mono<T> validate(T t) {
		if(t == null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		}
		
		Set<ConstraintViolation<T>> violations = validator.validate(t);
		
        if( violations == null || violations.isEmpty() ) {
            return Mono.just(t);
        }

        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
	}

}
