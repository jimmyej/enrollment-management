package com.jse.services;

import com.jse.documents.Enrollment;

import reactor.core.publisher.Mono;

public interface EnrollmentService extends CrudService<Enrollment, String> {
	Mono<byte[]> generateEnrollmentReport(String enrollmentId);
}
