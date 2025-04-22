package com.mitocode.services;

import org.springframework.data.domain.Pageable;

import com.mitocode.services.helpers.PageHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CrudService<T, I> {
    Mono<T> save(T t);
    Mono<T> update(T t);
    Flux<T> findAll();
    Mono<T> findById(I id);
    Mono<Void> delete(I id);
    Mono<PageHelper<T>> findPage(Pageable page);
}
