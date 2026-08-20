package com.jse.services.impls;

import com.jse.documents.Course;
import com.jse.repositories.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    CourseRepository repository;

    @InjectMocks
    CourseServiceImpl service;

    private Course c1;

    @BeforeEach
    void setUp() {
        c1 = new Course();
        c1.setId("1");
        c1.setName("Math");
        c1.setAcronym("MATH");
        c1.setStatus(true);
    }

    @Test
    void saveCourse() {
        when(repository.save(c1)).thenReturn(Mono.just(c1));

        StepVerifier.create(service.save(c1))
                .expectNextMatches(st -> st.getId().equals("1") && st.getName().equals("Math"))
                .verifyComplete();

        verify(repository).save(c1);
    }

    @Test
    void findAllCourses() {
        when(repository.findAll()).thenReturn(Flux.just(c1));

        StepVerifier.create(service.findAll())
                .expectNextMatches(st -> st.getId().equals("1"))
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void findById() {
        when(repository.findById("1")).thenReturn(Mono.just(c1));

        StepVerifier.create(service.findById("1"))
                .expectNextMatches(st -> st.getName().equals("Math"))
                .verifyComplete();

        verify(repository).findById("1");
    }

    @Test
    void deleteById() {
        when(repository.deleteById("1")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("1"))
                .verifyComplete();

        verify(repository).deleteById("1");
    }

    @Test
    void findPage() {
        Course c2 = new Course();
        c2.setId("2");
        c2.setName("Physics");

        when(repository.findAll()).thenReturn(Flux.just(c1, c2));

        Pageable pageReq = PageRequest.of(0, 1);

        StepVerifier.create(service.findPage(pageReq))
                .expectNextMatches(page -> page.getContent().size() == 1 && page.first())
                .verifyComplete();

        verify(repository).findAll();
    }
}
