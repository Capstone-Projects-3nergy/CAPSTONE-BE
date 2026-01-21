package com.nw2.parcel.configs;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", System.getenv().getOrDefault("CLOUDINARY_CLOUD_NAME", "dfyouxpnu"));
        config.put("api_key", System.getenv().getOrDefault("CLOUDINARY_API_KEY", "942824938494116"));
        config.put("api_secret", System.getenv().getOrDefault("CLOUDINARY_API_SECRET", "EUVITbtwuWoJwLrj9-J2vSjlgI0"));
        return new Cloudinary(config);
    }
}
//