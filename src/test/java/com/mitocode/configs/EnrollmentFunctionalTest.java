package com.mitocode.configs;

import com.mitocode.EnrollmentManagementApplication;
import com.mitocode.documents.Course;
import com.mitocode.documents.Enrollment;
import com.mitocode.documents.Student;
import com.mitocode.handlers.EnrollmentHandler;
import com.mitocode.repositories.EnrollmentRepository;
import com.mitocode.services.impls.EnrollmentServiceImpl;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EnrollmentManagementApplication.class)
@Import(EnrollmentServiceImpl.class)
public class EnrollmentFunctionalTest {
    @Autowired
    private RouterConfig config;

    @Autowired
    private EnrollmentHandler enrollmentHandler;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    private List<Enrollment> enrollments;
    private List<Course> courses;
    private List<Student> students;

    @BeforeEach
    void init() {
        Student student1 = new Student("1","Angel", "Felix", "12345678", 24, "", "");
        Student student2 = new Student("2","Jimmy", "Sanchez", "87654321", 35, "", "");
        Student student3 = new Student("3","Tony", "Sanchez", "12312312", 26, "", "");
        students = new ArrayList<>(Arrays.asList(student1, student2, student3));

        Course course1 = new Course("1","Java", "JV", true);
        Course course2 = new Course("2","Python", "PY", true);
        Course course3 = new Course("3","React", "RT", true);
        courses = new ArrayList<>(Arrays.asList(course1, course2, course3));

        Enrollment enrollment1 = new Enrollment("1", LocalDateTime.now(), student1, courses, true);
        Enrollment enrollment2 = new Enrollment("2",LocalDateTime.now(), student2, courses, true);
        Enrollment enrollment3 = new Enrollment("3",LocalDateTime.now(), student1, courses, true);
        enrollments = new ArrayList<>(Arrays.asList(enrollment1, enrollment2, enrollment3));
    }

    @Test
    void whenGetAllEnrollments_thenCorrectEnrollment() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.enrollmentRoutes(enrollmentHandler))
                .build();

        Flux<Enrollment> enrollmentFlux = Flux.fromIterable(enrollments);
        given(enrollmentRepository.findAll()).willReturn(enrollmentFlux);

        client.get()
                .uri("/api/v1/enrollments")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Enrollment.class)
                .isEqualTo(enrollments);
    }

    @Test
    void whenGetEnrollmentsByPage_thenCorrectEnrollment() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.enrollmentRoutes(enrollmentHandler))
                .build();

        Flux<Enrollment> enrollmentFlux = Flux.fromIterable(enrollments);
        given(enrollmentRepository.findAll()).willReturn(enrollmentFlux);

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/enrollments/pages")
                        .queryParam("page", "0")
                        .queryParam("size", "2")
                        .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Enrollment.class);
    }

    @Test
    void givenEnrollmentId_whenGetEnrollmentById_thenCorrectEnrollment() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.enrollmentRoutes(enrollmentHandler))
                .build();

        Enrollment enrollment = enrollments.get(0);

        given(enrollmentRepository.findById("1")).willReturn(Mono.just(enrollment));

        client.get()
                .uri("/api/v1/enrollments/1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Enrollment.class)
                .isEqualTo(enrollment);
    }

    @Test
    void givenCreateEnrollment_thenEnrollmentCreated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.enrollmentRoutes(enrollmentHandler))
                .build();

        Enrollment enrollment = new Enrollment("1", LocalDateTime.now(), students.get(0), courses, true);

        given(enrollmentRepository.save(enrollment)).willReturn(Mono.just(enrollment));

        client.method(HttpMethod.POST)
                .uri("/api/v1/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(enrollment), Enrollment.class)
                .exchange()
                .expectStatus()
                .isCreated();

        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void givenUpdateEnrollment_thenEnrollmentUpdated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.enrollmentRoutes(enrollmentHandler))
                .build();

        Enrollment enrollment = new Enrollment("1", LocalDateTime.now(), students.get(0), courses, true);

        given(enrollmentRepository.findById(enrollment.getId())).willReturn(Mono.just(enrollment));
        given(enrollmentRepository.save(enrollment)).willReturn(Mono.just(enrollment));

        client.put()
                .uri("/api/v1/enrollments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(enrollment), Enrollment.class)
                .exchange()
                .expectStatus()
                .isOk();

        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void givenEnrollmentId_whenDeleteEnrollmentById_thenEnrollmentDeleted() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.enrollmentRoutes(enrollmentHandler))
                .build();

        given(enrollmentRepository.findById("1")).willReturn(Mono.just(enrollments.get(0)));
        given(enrollmentRepository.deleteById("1")).willReturn(Mono.empty());

        client.delete()
                .uri("/api/v1/enrollments/1")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
