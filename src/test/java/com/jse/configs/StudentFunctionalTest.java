package com.jse.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.jse.EnrollmentManagementApplication;
import com.jse.documents.Student;
import com.jse.handlers.StudentHandler;
import com.jse.repositories.StudentRepository;
import com.jse.services.impls.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unused")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EnrollmentManagementApplication.class)
@Import(StudentServiceImpl.class)
class StudentFunctionalTest {
    @Autowired
    private RouterConfig config;

    @Autowired
    private StudentHandler studentHandler;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private MediaConfig mediaConfig;

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

        Student student = students.getFirst();

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

        given(studentRepository.findById("1")).willReturn(Mono.just(students.getFirst()));
        given(studentRepository.deleteById("1")).willReturn(Mono.empty());

        client.delete()
                .uri("/api/v1/students/1")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void givenUploadStudentPhoto_thenStudentUpdated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        Student student = students.getFirst();
        Cloudinary cloudinary = org.mockito.Mockito.mock(Cloudinary.class);
        Uploader uploader = org.mockito.Mockito.mock(Uploader.class);

        given(studentRepository.findById("1")).willReturn(Mono.just(student));
        given(studentRepository.save(any(Student.class))).willReturn(Mono.just(student));
        given(mediaConfig.hasAnyCloudinaryConfig()).willReturn(true);
        given(mediaConfig.cloudinaryConfig()).willReturn(cloudinary);
        given(cloudinary.uploader()).willReturn(uploader);
        try {
            given(uploader.upload(any(java.io.File.class), anyMap())).willReturn(Map.of(
                    "url", "https://res.cloudinary.com/demo/student-photo.jpg",
                    "public_id", "student-public-id"
            ));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error stubbing Cloudinary upload", e);
        }

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file",
                        new ByteArrayResource("fake-image-content".getBytes(StandardCharsets.UTF_8)))
                .filename("student-photo.jpg")
                .contentType(MediaType.IMAGE_JPEG);

        client.post()
                .uri("/api/v1/students/1/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Student.class)
                .value(studentResponse -> assertNotNull(studentResponse.getUrlPhoto()));

        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void givenUnsupportedCloudinaryConfig_whenUploadStudentPhoto_thenInternalServerError() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.studentRoutes(studentHandler))
                .build();

        given(studentRepository.findById("1")).willReturn(Mono.just(students.getFirst()));
        given(mediaConfig.hasAnyCloudinaryConfig()).willReturn(false);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file",
                        new ByteArrayResource("fake-image-content".getBytes(StandardCharsets.UTF_8)))
                .filename("student-photo.jpg")
                .contentType(MediaType.IMAGE_JPEG);

        client.post()
                .uri("/api/v1/students/1/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Cloudinary not configured. Set CLOUD_NAME/API_KEY/API_SECRET env vars or CLOUDINARY_URL.");
    }

}
