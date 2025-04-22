package com.mitocode.services.impls;

import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;

import com.mitocode.repositories.CrudRepository;
import com.mitocode.services.CrudService;
import com.mitocode.services.helpers.PageHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CrudServiceImpl<T, I> implements CrudService<T, I> {
	
	protected abstract CrudRepository<T, I> getCrudRepository();
	
    public Mono<T> save(T t) {
        return getCrudRepository().save(t);
    }

    public Mono<T> update(T t) {
        return getCrudRepository().save(t);
    }

    public Flux<T> findAll() {
        return getCrudRepository().findAll();
    }

    public Mono<T> findById(I id) {
        return getCrudRepository().findById(id);
    }

    public Mono<Void> delete(I id) {
        return getCrudRepository().deleteById(id);
    }
    
    public Mono<PageHelper<T>> findPage(Pageable page){
        return getCrudRepository().findAll()
                .collectList()
                .map(list -> 
                	new PageHelper<>(
                        list.stream()
                                .skip((long) page.getPageNumber() * page.getPageSize())
                                .limit(page.getPageSize())
                                .collect(Collectors.toList()),
                        page.getPageNumber(), page.getPageSize(), list.size()
                    )
                );
    }
}
