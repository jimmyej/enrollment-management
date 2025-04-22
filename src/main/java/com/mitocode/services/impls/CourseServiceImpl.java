package com.mitocode.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitocode.documents.Course;
import com.mitocode.repositories.CourseRepository;
import com.mitocode.repositories.CrudRepository;
import com.mitocode.services.CourseService;

@Service
public class CourseServiceImpl extends CrudServiceImpl<Course, String> implements CourseService {

	private final CourseRepository courseRepository;

	@Autowired
	public CourseServiceImpl(CourseRepository courseRepository){
		this.courseRepository = courseRepository;
	}

	protected CrudRepository<Course, String> getCrudRepository() {
		return courseRepository;
	}
}
