package com.mitocode.services.impls;

import com.mitocode.documents.Student;
import com.mitocode.repositories.StudentRepository;
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
class StudentServiceImplTest {

    @Mock
    StudentRepository repository;

    @InjectMocks
    StudentServiceImpl service;

    private Student s1;

    @BeforeEach
    void setUp() {
        s1 = new Student();
        s1.setId("1");
        s1.setFirstName("John");
        s1.setLastName("Doe");
        s1.setDocNumber("ABC123");
        s1.setAge(25);
    }

    @Test
    void saveStudent() {
        when(repository.save(s1)).thenReturn(Mono.just(s1));

        StepVerifier.create(service.save(s1))
                .expectNextMatches(st -> st.getId().equals("1") && st.getFirstName().equals("John"))
                .verifyComplete();

        verify(repository).save(s1);
    }

    @Test
    void findAllStudents() {
        when(repository.findAll()).thenReturn(Flux.just(s1));

        StepVerifier.create(service.findAll())
                .expectNextMatches(st -> st.getId().equals("1"))
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void findById() {
        when(repository.findById("1")).thenReturn(Mono.just(s1));

        StepVerifier.create(service.findById("1"))
                .expectNextMatches(st -> st.getFirstName().equals("John"))
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
        // create two students
        Student s2 = new Student();
        s2.setId("2");
        s2.setFirstName("Jane");

        when(repository.findAll()).thenReturn(Flux.just(s1, s2));

        Pageable pageReq = PageRequest.of(0, 1);

        StepVerifier.create(service.findPage(pageReq))
                .expectNextMatches(page -> page.getContent().size() == 1 && page.first())
                .verifyComplete();

        verify(repository).findAll();
    }
}
