package com.mitocode.repositories;

import com.mitocode.documents.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, String>{

}
