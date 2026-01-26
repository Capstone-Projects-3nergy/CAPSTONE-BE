//package com.nw2.parcel.services;
//
//import com.google.cloud.storage.Blob;
//import com.google.cloud.storage.Bucket;
//import com.google.firebase.cloud.StorageClient;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.UUID;
//
//@Service
//public class FileStorageService {
//
//    private static final String PROFILE_FOLDER = "profile-images";
//
//    /**
//     * อัปโหลดรูปโปรไฟล์
//     */
//    public String uploadProfileImage(MultipartFile file, Integer userId) {
//        try {
//            Bucket bucket = StorageClient.getInstance().bucket();
//
//            String fileName = PROFILE_FOLDER + "/user_" + userId + "/"
//                    + UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//            Blob blob = bucket.create(
//                    fileName,
//                    file.getBytes(),
//                    file.getContentType()
//            );
//
//            // ทำให้ไฟล์ public
//            blob.createAcl(com.google.cloud.storage.Acl.of(
//                    com.google.cloud.storage.Acl.User.ofAllUsers(),
//                    com.google.cloud.storage.Acl.Role.READER
//            ));
//
//            // public url
//            return String.format(
//                    "https://storage.googleapis.com/%s/%s",
//                    bucket.getName(),
//                    fileName
//            );
//
//        } catch (Exception e) {
//            throw new RuntimeException("Upload profile image failed", e);
//        }
//    }
//
//    /**
//     * ลบไฟล์จาก Firebase Storage (ใช้ตอนเปลี่ยนรูป)
//     */
//    public void deleteFileByUrl(String fileUrl) {
//        try {
//            Bucket bucket = StorageClient.getInstance().bucket();
//
//            String filePath = fileUrl.substring(
//                    fileUrl.indexOf(bucket.getName()) + bucket.getName().length() + 1
//            );
//
//            Blob blob = bucket.get(filePath);
//            if (blob != null) {
//                blob.delete();
//            }
//        } catch (Exception e) {
//            // ไม่ throw ก็ได้ กันระบบพัง
//            System.err.println("Failed to delete old profile image: " + e.getMessage());
//        }
//    }
//}
package com.nw2.parcel.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Cloudinary cloudinary;

    /**
     * Upload profile image to Cloudinary
     */
    public String uploadProfileImage(MultipartFile file, Integer userId) {
        try {
            validateImage(file);

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "profile-images/user_" + userId,
                            "public_id", "avatar",
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload profile image failed", e);
        }
    }

    /**
     * Delete image by URL
     */
    public void deleteFileByUrl(String imageUrl) {
        try {
            if (imageUrl == null) return;

            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (Exception e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }
    }

    /**
     * Extract Cloudinary public_id from URL
     */
    private String extractPublicId(String url) {
        // example:
        // https://res.cloudinary.com/{cloud}/image/upload/v123/profile-images/user_1/avatar.jpg
        String[] parts = url.split("/");
        int uploadIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("upload")) {
                uploadIndex = i;
                break;
            }
        }
        if (uploadIndex == -1) return null;

        String path = String.join("/",
                java.util.Arrays.copyOfRange(parts, uploadIndex + 2, parts.length)
        );

        return path.replaceFirst("\\.[^.]+$", "");
    }
}