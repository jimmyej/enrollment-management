package com.jse.repositories;

import com.jse.documents.Enrollment;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends CrudRepository<Enrollment, String>{

}
