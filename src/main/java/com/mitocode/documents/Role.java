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
@Document( collection = "roles")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role {
	@Id
	private String id;
	private String name;
}
