package com.jse.repositories;

import com.jse.documents.Course;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends CrudRepository<Course, String>{

}
