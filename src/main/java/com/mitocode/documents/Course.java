package com.mitocode.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document( collection = "courses")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Course {
	@Id
	private String id;
	private String name;
	private String acronym;
	private boolean status;
}
