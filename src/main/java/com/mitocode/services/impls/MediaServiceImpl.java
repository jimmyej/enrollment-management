package com.mitocode.services.impls;

import com.cloudinary.utils.ObjectUtils;
import com.mitocode.configs.MediaConfig;
import com.mitocode.services.MediaService;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
public class MediaServiceImpl implements MediaService {

    Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

    @Autowired
    private MediaConfig mediaConfig;

    public JSONObject uploadImage(FilePart image, String publicId) {
        File file = null;
        if(image != null){
            try {
                String fileName = image.filename();
                file = Files.createTempFile("temp", fileName).toFile();
                image.transferTo(file).block();
                if(publicId != null && !publicId.isEmpty() && !publicId.equals("Optional.empty")){
                    logger.info("Deleting existing file: {}", publicId);
                    mediaConfig.cloudinaryConfig().uploader().destroy(publicId, ObjectUtils.emptyMap());
                }
                Map<String, Object> uploadResult = mediaConfig.cloudinaryConfig().uploader().upload(file, ObjectUtils.emptyMap());
                return new JSONObject(uploadResult);
            } catch (IOException e) {
                logger.error(e.getMessage());
                return null;
            } finally {
                if(file != null){
                    logger.info("Temporary file deleted: {}",  file.delete());
                }
            }
        } else {
            logger.warn("file not found");
            return null;
        }
    }
}
