package com.mitocode.repositories;

import com.mitocode.documents.User;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
	Mono<User> findOneByUsername(String username);
}
