package com.mitocode.configs;

import com.mitocode.EnrollmentManagementApplication;
import com.mitocode.documents.Student;
import com.mitocode.handlers.StudentHandler;
import com.mitocode.repositories.StudentRepository;
import com.mitocode.services.impls.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EnrollmentManagementApplication.class)
@Import(StudentServiceImpl.class)
public class StudentFunctionalTest {
    @Autowired
    private RouterConfig config;

    @Autowired
    private StudentHandler studentHandler;

    @MockBean
    private StudentRepository studentRepository;

    private List<Student> students;

    @BeforeEach
    void init() {
        Student student1 = new Student("1","Angel", "Felix", "12345678", 24, "", "");
        Student student2 = new Student("2","Jimmy", "Sanchez", "87654321", 35, "", "");
        Student student3 = new Student("3","Tony", "Sanchez", "12312312", 26, "", "");
        students = new ArrayList<>(Arrays.asList(student1, student2, student3));
    }

    @Test
    void whenGetAllStudents_thenCorrectStudent() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        Flux<Student> studentFlux = Flux.fromIterable(students);
        given(studentRepository.findAll()).willReturn(studentFlux);

        client.get()
                .uri("/api/v1/students")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Student.class)
                .isEqualTo(students);
    }

    @Test
    void whenGetStudentsByPage_thenCorrectStudent() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        Flux<Student> studentFlux = Flux.fromIterable(students);
        given(studentRepository.findAll()).willReturn(studentFlux);

        client.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/v1/students/pages")
                    .queryParam("page", "0")
                    .queryParam("size", "2")
                    .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Student.class);
    }

    @Test
    void givenStudentId_whenGetStudentById_thenCorrectStudent() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        Student student = students.get(0);

        given(studentRepository.findById("1")).willReturn(Mono.just(student));

        client.get()
                .uri("/api/v1/students/1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Student.class)
                .isEqualTo(student);
    }

    @Test
    void givenCreateStudent_thenStudentCreated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        Student student = new Student("1", "Jimmy Eloy", "Sanchez Escalante", "44073426", 35, "", "");

        given(studentRepository.save(student)).willReturn(Mono.just(student));

        client.method(HttpMethod.POST)
                .uri("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(student), Student.class)
                .exchange()
                .expectStatus()
                .isCreated();

        verify(studentRepository).save(student);
    }

    @Test
    void givenUpdateStudent_thenStudentUpdated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        Student student = new Student("1", "Jimmy Eloy", "Sanchez Escalante", "44073426", 35, "", "");

        given(studentRepository.findById(student.getId())).willReturn(Mono.just(student));
        given(studentRepository.save(student)).willReturn(Mono.just(student));

        client.put()
                .uri("/api/v1/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(student), Student.class)
                .exchange()
                .expectStatus()
                .isOk();

        verify(studentRepository).save(student);
    }

    @Test
    void givenStudentId_whenDeleteStudentById_thenStudentDeleted() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        given(studentRepository.findById("1")).willReturn(Mono.just(students.get(0)));
        given(studentRepository.deleteById("1")).willReturn(Mono.empty());

        client.delete()
                .uri("/api/v1/students/1")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
