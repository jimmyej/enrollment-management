package com.jse.repositories;

import com.jse.documents.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, String>{

}
