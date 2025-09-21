package me.taeuk.record.service;

import me.taeuk.record.domain.YakuImage;
import me.taeuk.record.repository.YakuImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class YakuImageService {

    private final YakuImageRepository yakuImageRepository;
    private final Path uploadDir = Paths.get("uploads/yakuImages").toAbsolutePath().normalize();

    public YakuImageService(YakuImageRepository yakuImageRepository) {
        this.yakuImageRepository = yakuImageRepository;
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패", e);
        }
    }

    public YakuImage saveImageInfo(String nickname, String yaku, String imageUrl) {
        YakuImage yakuImage = new YakuImage(nickname, yaku, imageUrl);
        return yakuImageRepository.save(yakuImage);
    }

    public List<YakuImage> getImagesByNickname(String nickname) {
        return yakuImageRepository.findByNickname(nickname);
    }

    @Transactional
    public void deleteImageInfo(String nickname, String yaku) throws IOException {
        // DB에서 해당 닉네임+역만의 이미지 정보 조회
        List<YakuImage> images = yakuImageRepository.findByNicknameAndYaku(nickname, yaku);

        for (YakuImage img : images) {
            // 실제 파일 삭제
            String imageUrl = img.getImageUrl(); // 예: "/uploads/yakuImages/filename.jpg"
            Path filePath = resolveFilePath(imageUrl);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            // DB 레코드 삭제
            yakuImageRepository.delete(img);
        }
    }

    private Path resolveFilePath(String imageUrl) {
        // imageUrl이 /uploads/yakuImages/filename.jpg 형태라고 가정
        // 업로드 디렉토리 절대경로와 결합해 실제 파일 경로 반환
        String relativePath = imageUrl.replaceFirst("^/uploads/yakuImages/", "");
        return uploadDir.resolve(relativePath).normalize();
    }
}
