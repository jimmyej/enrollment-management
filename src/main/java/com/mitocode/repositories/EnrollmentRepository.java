package com.mitocode.repositories;

import com.mitocode.documents.Enrollment;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends CrudRepository<Enrollment, String>{

}
