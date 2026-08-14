package com.mitocode.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaConfig {

    private static final Logger logger = LoggerFactory.getLogger(MediaConfig.class);

    // Provide empty defaults so Spring won't fail resolving the placeholders.
    @Value("${CLOUD_NAME:}")
    private String cloudName;

    @Value("${API_KEY:}")
    private String apiKey;

    @Value("${API_SECRET:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinaryConfig() {
        // If explicit Cloudinary vars are provided, use them. Otherwise return a default Cloudinary
        // which will attempt to read CLOUDINARY_URL or other env-based configuration.
        if (cloudName == null || cloudName.isEmpty() || apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
            logger.warn("Cloudinary credentials not fully provided via CLOUD_NAME/API_KEY/API_SECRET. Falling back to default Cloudinary config (CLOUDINARY_URL or environment). Set variables or .env to avoid this warning.");
            return new Cloudinary();
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    // Helper to signal whether explicit credentials are available
    public boolean hasCloudinaryCredentials() {
        return cloudName != null && !cloudName.isEmpty()
                && apiKey != null && !apiKey.isEmpty()
                && apiSecret != null && !apiSecret.isEmpty();
    }

    // True if either explicit credentials or CLOUDINARY_URL env var exists
    public boolean hasAnyCloudinaryConfig() {
        return hasCloudinaryCredentials() || System.getenv("CLOUDINARY_URL") != null;
    }
}
