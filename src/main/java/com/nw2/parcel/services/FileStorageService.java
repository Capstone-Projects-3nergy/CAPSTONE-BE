package com.nw2.parcel.services;

//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.UUID;

//@Service
//public class FileStorageService {

//    public String uploadProfileImage(MultipartFile file, Integer userId) {
//        // TODO: upload ไป S3 / Firebase Storage / Local
//        String filename = "profile_" + userId + "_" + UUID.randomUUID() + ".jpg";
//        return "https://cdn.example.com/profile/" + filename;
//    }

//}

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private Bucket getBucket() {
        return StorageClient.getInstance().bucket();
    }

    public String uploadProfileImage(MultipartFile file, Integer userId) {

        try {
            String extension = getExtension(file.getOriginalFilename());

            String filePath = String.format(
                    "profiles/user_%d/profile_%d_%s%s",
                    userId,
                    userId,
                    UUID.randomUUID(),
                    extension
            );

            Bucket bucket = getBucket();

            Blob blob = bucket.create(
                    filePath,
                    file.getBytes(),
                    file.getContentType()
            );

            // ทำให้ public
//            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            // URL ที่ frontend ใช้ได้เลย
            return String.format(
                    "https://storage.googleapis.com/%s/%s",
                    bucket.getName(),
                    filePath
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            Bucket bucket = getBucket();
            String bucketName = bucket.getName();

            String filePath = fileUrl.replace(
                    "https://storage.googleapis.com/" + bucketName + "/", ""
            );

            Blob blob = bucket.get(filePath);

            if (blob == null) {
                System.out.println("⚠️ File not found in bucket: " + filePath);
                return; // 🔥 ห้าม throw
            }

            blob.delete();

        } catch (Exception e) {
            // ❌ ห้าม throw RuntimeException
            System.out.println("⚠️ Ignore delete error: " + e.getMessage());
        }
    }
}
