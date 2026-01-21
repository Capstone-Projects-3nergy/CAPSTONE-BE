package com.nw2.parcel.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileStorageService {

    private static final String PROFILE_FOLDER = "profile-images";

    /**
     * อัปโหลดรูปโปรไฟล์
     */
    public String uploadProfileImage(MultipartFile file, Integer userId) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();

            String fileName = PROFILE_FOLDER + "/user_" + userId + "/"
                    + UUID.randomUUID() + "_" + file.getOriginalFilename();

            Blob blob = bucket.create(
                    fileName,
                    file.getBytes(),
                    file.getContentType()
            );

            // ทำให้ไฟล์ public
            blob.createAcl(com.google.cloud.storage.Acl.of(
                    com.google.cloud.storage.Acl.User.ofAllUsers(),
                    com.google.cloud.storage.Acl.Role.READER
            ));

            // public url
            return String.format(
                    "https://storage.googleapis.com/%s/%s",
                    bucket.getName(),
                    fileName
            );

        } catch (Exception e) {
            throw new RuntimeException("Upload profile image failed", e);
        }
    }

    /**
     * ลบไฟล์จาก Firebase Storage (ใช้ตอนเปลี่ยนรูป)
     */
    public void deleteFileByUrl(String fileUrl) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();

            String filePath = fileUrl.substring(
                    fileUrl.indexOf(bucket.getName()) + bucket.getName().length() + 1
            );

            Blob blob = bucket.get(filePath);
            if (blob != null) {
                blob.delete();
            }
        } catch (Exception e) {
            // ไม่ throw ก็ได้ กันระบบพัง
            System.err.println("Failed to delete old profile image: " + e.getMessage());
        }
    }
}
