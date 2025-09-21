package me.taeuk.record.controller;

import me.taeuk.record.dto.YakuImageDto;
import me.taeuk.record.service.FileStorageService;
import me.taeuk.record.service.YakuImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ImageUploadController {

    private final FileStorageService fileStorageService;
    private final YakuImageService yakuImageService;

    public ImageUploadController(FileStorageService fileStorageService,
                                 YakuImageService yakuImageService) {
        this.fileStorageService = fileStorageService;
        this.yakuImageService = yakuImageService;
    }

    @PostMapping("/uploadYakuImage")
    public ResponseEntity<Map<String, String>> uploadYakuImage(
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "nickname", required = true) String nickname,
            @RequestParam(value = "yaku", required = true) String yaku) {
        try {
            // 1. 파일 저장 (물리적 저장 및 URL 반환)
            String storedFileUrl = fileStorageService.storeFile(file, nickname, yaku);

            // 2. DB에 이미지 메타 정보 저장 (닉네임, yaku, 파일 URL 등)
            yakuImageService.saveImageInfo(nickname, yaku, storedFileUrl);

            // 3. 클라이언트에 저장된 이미지 URL 반환
            return ResponseEntity.ok(Map.of("url", storedFileUrl));
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 에러 출력
            return ResponseEntity.status(500).body(Map.of("error", "파일 업로드 실패"));
        }
    }

    @DeleteMapping("/deleteYakuImage")
    public ResponseEntity<Map<String, String>> deleteYakuImage(
            @RequestParam(value = "nickname", required = true) String nickname,
            @RequestParam(value = "yaku", required = true) String yaku) {
        try {
            // DB 및 저장소에서 이미지 메타 및 파일 삭제
            yakuImageService.deleteImageInfo(nickname, yaku);

            return ResponseEntity.ok(Map.of("message", "이미지 삭제 성공"));
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그 출력
            return ResponseEntity.status(500).body(Map.of("error", "이미지 삭제 실패"));
        }
    }

    @GetMapping("/yaku-images")
    public ResponseEntity<List<YakuImageDto>> getYakuImagesByNickname(@RequestParam String nickname) {
        List<YakuImageDto> dtos = yakuImageService.getImagesByNickname(nickname).stream()
                .map(img -> new YakuImageDto(img.getYaku(), img.getImageUrl()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
