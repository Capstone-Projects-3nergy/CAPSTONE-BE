package com.nw2.parcel.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileStorageService {

    public String uploadProfileImage(MultipartFile file, Integer userId) {
        // TODO: upload ไป S3 / Firebase Storage / Local
        String filename = "profile_" + userId + "_" + UUID.randomUUID() + ".jpg";
        return "https://cdn.example.com/profile/" + filename;
    }
}
