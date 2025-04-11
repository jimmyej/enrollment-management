package com.mitocode.services;

import com.mitocode.documents.Enrollment;

import reactor.core.publisher.Mono;

public interface EnrollmentService extends CrudService<Enrollment, String> {
	Mono<byte[]> generateEnrollmentReport(String enrollmentId);
}
