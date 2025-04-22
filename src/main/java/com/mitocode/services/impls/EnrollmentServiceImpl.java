package com.mitocode.services.impls;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitocode.documents.Enrollment;
import com.mitocode.repositories.CourseRepository;
import com.mitocode.repositories.CrudRepository;
import com.mitocode.repositories.EnrollmentRepository;
import com.mitocode.repositories.StudentRepository;
import com.mitocode.services.EnrollmentService;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EnrollmentServiceImpl extends CrudServiceImpl<Enrollment, String> implements EnrollmentService {

	Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

	private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

	@Autowired
	public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository, CourseRepository courseRepository){
		this.enrollmentRepository = enrollmentRepository;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
	}

	protected CrudRepository<Enrollment, String> getCrudRepository() {
		return enrollmentRepository;
	}

	@Override
	public Mono<byte[]> generateEnrollmentReport(String enrollmentId) {
		//Mono<Enrollment>
		return enrollmentRepository.findById(enrollmentId)
                //Getting student
                .flatMap(inv -> Mono.just(inv)
                        .zipWith(studentRepository.findById(inv.getStudent().getId()), (invo, cl) -> {
                            invo.setStudent(cl);
                            return invo;
                        })
                )
		        //Getting course
		        .flatMap(inv -> Flux.fromIterable(inv.getCourses()).flatMap(item -> courseRepository.findById(item.getId())
                        .map(p -> {
                            item.setId(p.getId());
                            item.setName(p.getName());
                            item.setAcronym(p.getAcronym());
                            item.setStatus(p.isStatus());
                            return item;
                        })).collectList().flatMap(list -> {
                    inv.setCourses(list);
                    return Mono.just(inv);
                })).map(f -> {
                    InputStream stream;
                    try {
                        Map<String, Object> parameters = new HashMap<>();
						parameters.put("student_name", f.getStudent().getFirstName() + " " + f.getStudent().getLastName());

                        stream = getClass().getResourceAsStream("/students_report.jrxml");
                        JasperReport report = JasperCompileManager.compileReport(stream);
                        JasperPrint print = JasperFillManager.fillReport(report, parameters, new JRBeanCollectionDataSource(f.getCourses()));
                        return JasperExportManager.exportReportToPdf(print);
                    } catch (Exception e) {
						logger.error(e.getMessage());
                    }
                    return new byte[0];
                });

    }

}
