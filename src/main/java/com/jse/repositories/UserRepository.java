package com.jse.repositories;

import com.jse.documents.User;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
	Mono<User> findOneByUsername(String username);
}
