package syu.likealion3.hackathon.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads}")
    private String urlPrefix;

    private static final Set<String> EXT_ALLOWED =
            Set.of("jpg","jpeg","png","gif","webp","bmp","heic","heif");

    private static final Map<String,String> CT2EXT = Map.ofEntries(
            Map.entry("image/jpeg","jpg"),
            Map.entry("image/jpg","jpg"),
            Map.entry("image/png","png"),
            Map.entry("image/gif","gif"),
            Map.entry("image/webp","webp"),
            Map.entry("image/bmp","bmp"),
            Map.entry("image/heic","heic"),
            Map.entry("image/heif","heif")
    );

    /** 이미지 저장 후 공개 URL 반환 (/uploads/xxxx.ext) */
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 1) content-type 우선 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("이미지 파일만 업로드할 수 있습니다.");
        }

        // 2) 확장자 결정: content-type → 원본 파일명 → 기본값
        String ext = CT2EXT.get(contentType);
        if (ext == null) {
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
            }
        }
        if (ext == null || !EXT_ALLOWED.contains(ext)) {
            throw new IOException("지원하지 않는 이미지 형식입니다. (허용: jpg, jpeg, png, gif, webp, bmp, heic, heif)");
        }

        // 3) 파일명/경로 준비
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(base);
        Path target = base.resolve(filename);

        // 4) 저장
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        }

        return urlPrefix + "/" + filename;
    }
}
