package com.mitocode.controllers;

import com.mitocode.documents.Student;
import com.mitocode.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v2/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }

    @PutMapping(value = {"/{id}/upload", "/{id}/upload/{publicId}"})
    Student uploadPhoto(@PathVariable String id, @PathVariable(required = false) String publicId, @RequestParam MultipartFile file){
        return null;//studentService.uploadPhoto(id, file, publicId);
    }
}
