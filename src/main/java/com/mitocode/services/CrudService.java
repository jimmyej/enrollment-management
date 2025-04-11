package com.mitocode.services;

import org.springframework.data.domain.Pageable;

import com.mitocode.services.helpers.PageHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CrudService<T, ID> {
    Mono<T> save(T t);
    Mono<T> update(T t);
    Flux<T> findAll();
    Mono<T> findById(ID id);
    Mono<Void> delete(ID id);
    Mono<PageHelper<T>> findPage(Pageable page);
}
