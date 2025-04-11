package com.mitocode.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitocode.documents.Student;
import com.mitocode.repositories.CrudRepository;
import com.mitocode.repositories.StudentRepository;
import com.mitocode.services.StudentService;

@Service
public class StudentServiceImpl extends CrudServiceImpl<Student, String> implements StudentService {
	
	@Autowired
	private StudentRepository customerRepository;

	protected CrudRepository<Student, String> getCrudRepository() {
		return customerRepository;
	}
}
