package com.jse.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jse.documents.Student;
import com.jse.repositories.CrudRepository;
import com.jse.repositories.StudentRepository;
import com.jse.services.StudentService;

@Service
public class StudentServiceImpl extends CrudServiceImpl<Student, String> implements StudentService {

	private final StudentRepository customerRepository;

	@Autowired
	public StudentServiceImpl(StudentRepository customerRepository){
		this.customerRepository = customerRepository;
	}

	protected CrudRepository<Student, String> getCrudRepository() {
		return customerRepository;
	}
}
