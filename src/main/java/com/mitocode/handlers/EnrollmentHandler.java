package com.mitocode.handlers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mitocode.documents.Enrollment;
import com.mitocode.services.EnrollmentService;
import com.mitocode.validators.RequestValidator;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class EnrollmentHandler {

	private final EnrollmentService service;
    private final RequestValidator requestValidator;

	@Autowired
	public EnrollmentHandler(EnrollmentService service, RequestValidator requestValidator){
		this.service = service;
		this.requestValidator = requestValidator;
	}
    
    public Mono<ServerResponse> findAll(ServerRequest req) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Enrollment.class);
    }

	public Mono<ServerResponse> findPage(ServerRequest req) {
		int page = Integer.parseInt(req.queryParam("page").orElse("0"));
		int size = Integer.parseInt(req.queryParam("size").orElse("5"));
		Pageable pageRequest = PageRequest.of(page, size);
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findPage(pageRequest), Enrollment.class);
	}
    
    public Mono<ServerResponse> findById(ServerRequest req) {
    	String id = req.pathVariable("id");
        return service.findById(id)
                .flatMap(p -> ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(fromValue(p))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> create(ServerRequest req){
    	Mono<Enrollment> monoEnrollment = req.bodyToMono(Enrollment.class);
    	return monoEnrollment
    			.flatMap(requestValidator::validate)
    			.flatMap(service::save)
    			.flatMap( p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
    					.contentType(MediaType.APPLICATION_JSON)
    					.body(fromValue(p))
				);
    }
    
    public Mono<ServerResponse> update(ServerRequest req){
    	String id = req.pathVariable("id");
    	Mono<Enrollment> monoEnrollment = req.bodyToMono(Enrollment.class);
        Mono<Enrollment> monoBD = service.findById(id);
    	
    	return monoBD
    			.zipWith(monoEnrollment, (db, e) -> {
    				db.setId(e.getId());
    				db.setEnrollmentDate(e.getEnrollmentDate());
    				db.setStudent(e.getStudent());
    				db.setCourses(e.getCourses());
    				db.setStatus(e.isStatus());
    				return db;
    			})
                .flatMap(requestValidator::validate)
                .flatMap(service::update)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> delete(ServerRequest req){
    	String id = req.pathVariable("id");
        return service.findById(id)
                .flatMap(p -> service.delete(p.getId())
                    .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
