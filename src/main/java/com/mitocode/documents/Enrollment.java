package com.mitocode.documents;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document( collection = "enrollments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Enrollment {
	@Id
	private String id;
	private LocalDateTime enrollmentDate = LocalDateTime.now();
	private Student student;
	private List<Course> courses;
	private boolean status;
}
