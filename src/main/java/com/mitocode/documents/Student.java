package com.mitocode.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document( collection = "students")
public class Student {
	@Id
	private String id;
	private String firstName;
	private String lastName;
	private String docNumber;
	private double age;
	private String urlPhoto;
	private String publicId;
}
