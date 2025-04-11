package com.mitocode.services;

import org.cloudinary.json.JSONObject;
import org.springframework.http.codec.multipart.FilePart;


public interface MediaService {
    JSONObject uploadImage(FilePart image, String publicId);
}
