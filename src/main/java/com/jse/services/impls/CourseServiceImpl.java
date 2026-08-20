package com.jse.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jse.documents.Course;
import com.jse.repositories.CourseRepository;
import com.jse.repositories.CrudRepository;
import com.jse.services.CourseService;

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
