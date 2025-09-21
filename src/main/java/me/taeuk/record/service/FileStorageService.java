package me.taeuk.record.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {
    private final Path uploadDir = Paths.get("uploads/yakuImages");

    public FileStorageService() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public String storeFile(MultipartFile file, String nickname, String yaku) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IOException("유효하지 않은 파일 이름입니다.");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 파일명에 특수문자나 공백을 제거하는 로직 추가 가능
        String safeNickname = nickname.replaceAll("[^a-zA-Z0-9_-]", "");
        String safeYaku = yaku.replaceAll("[^a-zA-Z0-9_-]", "");

        String filename = safeNickname + "_" + safeYaku + "_" + System.currentTimeMillis() + ext;
        Path targetPath = uploadDir.resolve(filename);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 반환은 클라이언트에서 접근하기 위한 상대 URL 경로 형태로 예시 반환
        return "/uploads/yakuImages/" + filename;
    }
}
