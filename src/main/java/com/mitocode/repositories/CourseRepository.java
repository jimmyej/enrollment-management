package com.mitocode.repositories;

import com.mitocode.documents.Course;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends CrudRepository<Course, String>{

}
