package com.mitocode.documents;

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
@Document( collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
	@Id
	private String id;
	private String username;
	private String password;
	private Boolean status;
	private List<Role> roles;
}
