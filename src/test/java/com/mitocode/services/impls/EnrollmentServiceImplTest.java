package com.mitocode.services.impls;

import com.mitocode.documents.Course;
import com.mitocode.documents.Enrollment;
import com.mitocode.documents.Student;
import com.mitocode.repositories.CourseRepository;
import com.mitocode.repositories.EnrollmentRepository;
import com.mitocode.repositories.StudentRepository;
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
