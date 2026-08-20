package com.jse.handlers;

import java.net.URI;

import com.cloudinary.utils.ObjectUtils;
import com.jse.configs.MediaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jse.documents.Student;
import com.jse.services.StudentService;
import com.jse.validators.RequestValidator;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class StudentHandler {

	Logger logger = LoggerFactory.getLogger(StudentHandler.class);

	private final StudentService studentService;
	private final MediaConfig mediaConfig;
    private final RequestValidator requestValidator;

	private static final String PUBLIC_ID_LABEL = "public_id";
	private static final String ITEM_ID = "id";

	@Autowired
	public StudentHandler(StudentService studentService, MediaConfig mediaConfig, RequestValidator requestValidator){
		this.studentService = studentService;
		this.mediaConfig = mediaConfig;
		this.requestValidator = requestValidator;
	}
    
    public Mono<ServerResponse> findAll(ServerRequest req) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(studentService.findAll(), Student.class);
    }

	public Mono<ServerResponse> findPage(ServerRequest req) {
		int page = Integer.parseInt(req.queryParam("page").orElse("0"));
		int size = Integer.parseInt(req.queryParam("size").orElse("5"));
		Pageable pageRequest = PageRequest.of(page, size);
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(studentService.findPage(pageRequest), Student.class);
	}
    
    public Mono<ServerResponse> findById(ServerRequest req) {
    	String id = req.pathVariable(ITEM_ID);
        return studentService.findById(id)
                .flatMap(p -> ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(fromValue(p))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> create(ServerRequest req){
    	Mono<Student> monoStudent = req.bodyToMono(Student.class);
    	return monoStudent
    			.flatMap(requestValidator::validate)
    			.flatMap(studentService::save)
    			.flatMap( p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
    					.contentType(MediaType.APPLICATION_JSON)
    					.body(fromValue(p))
				);
    }
    
    public Mono<ServerResponse> update(ServerRequest req){
    	String id = req.pathVariable(ITEM_ID);
    	Mono<Student> monoStudent = req.bodyToMono(Student.class);
        Mono<Student> monoBD = studentService.findById(id);
    	
    	return monoBD
    			.zipWith(monoStudent, (db, s) -> {
    				db.setId(s.getId());
    				db.setFirstName(s.getFirstName());
    				db.setLastName(s.getLastName());
    				db.setDocNumber(s.getDocNumber());
    				db.setAge(s.getAge());
					db.setUrlPhoto(s.getUrlPhoto());
					db.setPublicId(s.getPublicId());
    				return db;
    			})
                .flatMap(requestValidator::validate)
                .flatMap(studentService::update)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> delete(ServerRequest req){
    	String id = req.pathVariable(ITEM_ID);
        return studentService.findById(id)
                .flatMap(p -> studentService.delete(p.getId())
                    .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

	public Mono<ServerResponse> uploadReactive(ServerRequest req) {
		String id = req.pathVariable(ITEM_ID);
		Mono<Student> monoBD = studentService.findById(id);
		if (!mediaConfig.hasAnyCloudinaryConfig()) {
			return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.TEXT_PLAIN)
				.body(fromValue("Cloudinary not configured. Set CLOUD_NAME/API_KEY/API_SECRET env vars or CLOUDINARY_URL."));
		}

		Mono<java.io.File> uploadedFileMono = req.body(BodyExtractors.toMultipartData())
			.flatMap(parts -> {
				org.springframework.util.MultiValueMap<String, Part> map = parts;
				Part part = map.toSingleValueMap().get("file");
				if (!(part instanceof FilePart)) {
					return Mono.error(new IllegalArgumentException("Missing file part named 'file'"));
				}
				FilePart filePart = (FilePart) part;
				return Mono.fromCallable(() -> java.nio.file.Files.createTempFile("upload-", filePart.filename()))
					.subscribeOn(Schedulers.boundedElastic())
					.flatMap(path -> filePart.transferTo(path).thenReturn(path.toFile()));
			});

		Mono<Student> studentFromUpload = uploadedFileMono.flatMap(file ->
			Mono.fromCallable(() -> {
				java.util.Map<String, Object> uploadResult = mediaConfig.cloudinaryConfig().uploader().upload(file, ObjectUtils.asMap("resource_type", "auto"));
				org.cloudinary.json.JSONObject json = new org.cloudinary.json.JSONObject(uploadResult);
				String url = json.getString("url");
				String publicIdValue = json.getString(PUBLIC_ID_LABEL);
				Student student = new Student();
				student.setUrlPhoto(url);
				student.setPublicId(publicIdValue);
				return student;
			}).subscribeOn(Schedulers.boundedElastic())
			.doFinally(signal -> {
				try { if (file != null && file.exists()) file.delete(); } catch (Exception e) { logger.error(e.getMessage()); }
			})
		);

		return monoBD
				.zipWith(studentFromUpload, (db, s) -> {
					db.setUrlPhoto(s.getUrlPhoto());
					db.setPublicId(s.getPublicId());
					return db;
				})
				.flatMap(studentService::update)
				.flatMap(p -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}
}
