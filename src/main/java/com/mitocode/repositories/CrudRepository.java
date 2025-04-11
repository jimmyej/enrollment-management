package com.mitocode.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CrudRepository<T, ID> extends ReactiveMongoRepository<T, ID> {

}
