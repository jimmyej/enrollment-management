package com.mitocode.documents;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document( collection = "userRoles")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRole {
	private String userId;
	private String roleId;
}
