package com.mitocode.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;

import com.cloudinary.utils.ObjectUtils;
import com.mitocode.configs.MediaConfig;
import com.mitocode.services.MediaService;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mitocode.documents.Student;
import com.mitocode.services.StudentService;
import com.mitocode.validators.RequestValidator;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class StudentHandler {

	Logger logger = LoggerFactory.getLogger(StudentHandler.class);
	
	@Autowired
	private StudentService service;

	@Autowired
	private MediaService mediaService;

	@Autowired
	private MediaConfig mediaConfig;

	@Autowired
    private RequestValidator requestValidator;
    
    public Mono<ServerResponse> findAll(ServerRequest req) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Student.class);
    }

	public Mono<ServerResponse> findPage(ServerRequest req) {
		int page = Integer.parseInt(req.queryParam("page").orElse("0"));
		int size = Integer.parseInt(req.queryParam("size").orElse("5"));
		Pageable pageRequest = PageRequest.of(page, size);
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findPage(pageRequest), Student.class);
	}
    
    public Mono<ServerResponse> findById(ServerRequest req) {
    	String id = req.pathVariable("id");
        return service.findById(id)
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
    			.flatMap(service::save)
    			.flatMap( p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
    					.contentType(MediaType.APPLICATION_JSON)
    					.body(fromValue(p))
				);
    }
    
    public Mono<ServerResponse> update(ServerRequest req){
    	String id = req.pathVariable("id");
    	Mono<Student> monoStudent = req.bodyToMono(Student.class);
        Mono<Student> monoBD = service.findById(id);
    	
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
                .flatMap(service::update)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> delete(ServerRequest req){
    	String id = req.pathVariable("id");
        return service.findById(id)
                .flatMap(p -> service.delete(p.getId())
                    .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

	public Mono<ServerResponse> upload2(ServerRequest req){
		String id = req.pathVariable("id");
		String publicId = req.queryParam("public_id").toString();

		Mono<Student> monoBD = service.findById(id);
		Mono<Student> monoStudent = req.body(BodyExtractors.toParts()).collectList().flatMap(parts -> {
			try {
				FilePart image = (FilePart) parts.get(0);

				JSONObject json = mediaService.uploadImage(image, publicId);
				String url = json.getString("url");
				String publicIdValue = json.getString("public_id");

				Student student = new Student();
				student.setUrlPhoto(url);
				student.setPublicId(publicIdValue);
				return Mono.just(student);

			} catch (Exception e) {
				return Mono.empty();
			}
		});

		return monoBD
				.zipWith(monoStudent, (db, s) -> {
					db.setUrlPhoto(s.getUrlPhoto());
					db.setPublicId(s.getPublicId());
					return db;
				})
				.flatMap(service::update)
				.flatMap(p -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> upload3(ServerRequest req){
		String id = req.pathVariable("id");
		String publicId = req.queryParam("public_id").toString();

		MultiValueMap<String, Part> formData = req.body(BodyExtractors.toMultipartData()).block();
		FilePart fp = (FilePart) formData.toSingleValueMap().get("file");
		Path path = Paths.get("/Users/jimmy/tmp/");
		Set<PosixFilePermission> filePerm = PosixFilePermissions.fromString("rwxrwxrwx");
		FileAttribute<Set<PosixFilePermission>> fileAttr = PosixFilePermissions.asFileAttribute(filePerm);

		File file = null;
		Student student = null;
		try {
			file = Files.createTempFile(path,"tmp", fp.filename(), fileAttr).toFile();
			fp.transferTo(file).block();

			Map<String, Object> uploadResult = mediaConfig.cloudinaryConfig().uploader().upload(file, ObjectUtils.asMap("resource_type", "auto"));
			JSONObject json = new JSONObject(uploadResult);
			String url = json.getString("url");
			String publicIdValue = json.getString("public_id");

			student = new Student();
			student.setUrlPhoto(url);
			student.setPublicId(publicIdValue);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			file.delete();
		}

		Mono<Student> monoBD = service.findById(id);
		Mono<Student> studentMono = Mono.just(student);

		return studentMono.zipWith(monoBD, (s, db) -> {
					db.setUrlPhoto(s.getUrlPhoto());
					db.setPublicId(s.getPublicId());
					return db;
				})
				.flatMap(service::update)
				.flatMap(p -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}

//	public Mono<String> uploadHandler(MultiValueMap<String, Part> parts){
//
//		return parts
//				//.filter(part -> part instanceof FilePart) // only retain file parts
//				.ofType(FilePart.class) // convert the flux to FilePart
//				.flatMap(fm -> {
//					try {
//						return fm.transferTo(Files.createTempFile("temp", fm.filename())).thenReturn();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					return null;
//				}).then
//	}

	public Mono<ServerResponse> upload(ServerRequest req){
		String id = req.pathVariable("id");
		String publicId = req.queryParam("public_id").toString();

		Mono<Student> monoBD = service.findById(id);
		Mono<Student> monoStudent = req.body(BodyExtractors.toMultipartData())//req.body(BodyExtractors.toParts()).collectList()

				.flatMap(parts -> {
					Map<String, Part> map = parts.toSingleValueMap();
					FilePart filePart = (FilePart) map.get("file");
					File file;
					try {
						/*file = Files.createTempFile("temp", filePart.filename()).toFile();
						filePart.transferTo(file);*/

						Path tempFile = Files.createTempFile("temp", filePart.filename());
						filePart.transferTo(tempFile);
						file = ResourceUtils.getFile(tempFile.toUri());

						return Mono.just(file);
					} catch (IOException e) {
						logger.error(e.getMessage());
						return Mono.error(e);
					}
				})
				.flatMap(
						f -> {
							Student student = null;
							if(publicId != null && !publicId.isEmpty() && !publicId.equals("Optional.empty")) {
								try {
									mediaConfig.cloudinaryConfig().uploader().destroy(publicId, ObjectUtils.emptyMap());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							try {
								Map<String, Object> uploadResult = mediaConfig.cloudinaryConfig().uploader().upload(f, ObjectUtils.emptyMap());
								JSONObject json = new JSONObject(uploadResult);
								String url = json.getString("url");
								String publicIdValue = json.getString("public_id");
								student = new Student();
								student.setUrlPhoto(url);
								student.setPublicId(publicIdValue);
								logger.info("Student", student.toString());
								//return Mono.just(student);
							} catch (IOException e) {
								logger.error(e.getMessage());
								return Mono.error(e);
							}

							return Mono.just(student);
						}
				).log();

		return monoBD
				.zipWith(monoStudent, (db, s) -> {
					db.setUrlPhoto(s.getUrlPhoto());
					db.setPublicId(s.getPublicId());
					return db;
				})
				.flatMap(service::update)
				.flatMap(p -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}
}
