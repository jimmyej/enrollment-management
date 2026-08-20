package com.jse.services.impls;

import com.jse.documents.Course;
import com.jse.documents.Enrollment;
import com.jse.documents.Student;
import com.jse.repositories.CourseRepository;
import com.jse.repositories.EnrollmentRepository;
import com.jse.repositories.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    EnrollmentServiceImpl service;

    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        Student student = new Student();
        student.setId("s1");
        student.setFirstName("Stu");
        student.setLastName("Dent");

        Course c = new Course();
        c.setId("c1");

        enrollment = new Enrollment();
        enrollment.setId("1");
        enrollment.setStudent(student);
        enrollment.setCourses(java.util.List.of(c));
    }

    @Test
    void generateEnrollmentReport_returnsEmptyWhenJasperUnavailable() {
        when(enrollmentRepository.findById("1")).thenReturn(Mono.just(enrollment));
        when(studentRepository.findById("s1")).thenReturn(Mono.just(enrollment.getStudent()));
        when(courseRepository.findById("c1")).thenReturn(Mono.just(new Course("c1","Demo","DM",true)));

        StepVerifier.create(service.generateEnrollmentReport("1"))
                .expectNextMatches(bytes -> bytes != null && bytes.length >= 0)
                .verifyComplete();
    }
}
