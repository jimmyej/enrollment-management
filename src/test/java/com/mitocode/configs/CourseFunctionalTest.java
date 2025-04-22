package com.mitocode.configs;

import com.mitocode.EnrollmentManagementApplication;
import com.mitocode.documents.Course;
import com.mitocode.handlers.CourseHandler;
import com.mitocode.repositories.CourseRepository;
import com.mitocode.services.impls.CourseServiceImpl;
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

@SuppressWarnings("unused")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EnrollmentManagementApplication.class)
@Import(CourseServiceImpl.class)
class CourseFunctionalTest {
    @Autowired
    private RouterConfig config;

    @Autowired
    private CourseHandler courseHandler;

    @MockBean
    private CourseRepository courseRepository;

    private List<Course> courses;

    @BeforeEach
    void init() {
        Course course1 = new Course("1","Java", "JV", true);
        Course course2 = new Course("2","Python", "PY", true);
        Course course3 = new Course("3","React", "RT", true);
        courses = new ArrayList<>(Arrays.asList(course1, course2, course3));
    }

    @Test
    void whenGetAllCourses_thenCorrectCourse() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.courseRoutes(courseHandler))
                .build();

        Flux<Course> courseFlux = Flux.fromIterable(courses);
        given(courseRepository.findAll()).willReturn(courseFlux);

        client.get()
                .uri("/api/v1/courses")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Course.class)
                .isEqualTo(courses);
    }

    @Test
    void whenGetCoursesByPage_thenCorrectCourses() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.courseRoutes(courseHandler))
                .build();

        Flux<Course> courseFlux = Flux.fromIterable(courses);
        given(courseRepository.findAll()).willReturn(courseFlux);

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses/pages")
                        .queryParam("page", "0")
                        .queryParam("size", "2")
                        .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Course.class);
    }

    @Test
    void givenCourseId_whenGetCourseById_thenCorrectCourse() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.courseRoutes(courseHandler))
                .build();

        Course course = courses.get(0);

        given(courseRepository.findById("1")).willReturn(Mono.just(course));

        client.get()
                .uri("/api/v1/courses/1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Course.class)
                .isEqualTo(course);
    }

    @Test
    void givenCreateCourse_thenCourseCreated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.courseRoutes(courseHandler))
                .build();

        Course course = new Course("1","Angular", "AG", true);

        given(courseRepository.save(course)).willReturn(Mono.just(course));

        client.method(HttpMethod.POST)
                .uri("/api/v1/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(course), Course.class)
                .exchange()
                .expectStatus()
                .isCreated();

        verify(courseRepository).save(course);
    }

    @Test
    void givenUpdateCourse_thenCourseUpdated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.courseRoutes(courseHandler))
                .build();

        Course course = new Course("1","Java", "JA", true);

        given(courseRepository.findById(course.getId())).willReturn(Mono.just(course));
        given(courseRepository.save(course)).willReturn(Mono.just(course));

        client.put()
                .uri("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(course), Course.class)
                .exchange()
                .expectStatus()
                .isOk();

        verify(courseRepository).save(course);
    }

    @Test
    void givenCourseId_whenDeleteCourseById_thenCourseDeleted() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.courseRoutes(courseHandler))
                .build();

        given(courseRepository.findById("1")).willReturn(Mono.just(courses.get(0)));
        given(courseRepository.deleteById("1")).willReturn(Mono.empty());

        client.delete()
                .uri("/api/v1/courses/1")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
