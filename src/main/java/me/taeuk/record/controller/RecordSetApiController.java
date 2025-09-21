package me.taeuk.record.controller;

import me.taeuk.record.domain.RecordSet;
import me.taeuk.record.dto.AddRecordsRequest;
import me.taeuk.record.dto.NicknameDetailResponse;
import me.taeuk.record.service.RecordSetService;
import me.taeuk.record.service.YakuImageService; // 새로 추가한 이미지 서비스
import me.taeuk.record.domain.YakuImage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recordset")
public class RecordSetApiController {

    private final RecordSetService recordSetService;
    private final YakuImageService yakuImageService; // 이미지 메타 정보 서비스

    public RecordSetApiController(RecordSetService recordSetService, YakuImageService yakuImageService) {
        this.recordSetService = recordSetService;
        this.yakuImageService = yakuImageService;
    }

    @PostMapping
    public ResponseEntity<?> createRecordSet(@RequestBody AddRecordsRequest request) {
        try {
            RecordSet saved = recordSetService.save(request.getRecords());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("기록 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<RecordSet> getLatestRecordSet() {
        try {
            RecordSet latest = recordSetService.getLatestRecordSet();
            return ResponseEntity.ok(latest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordSet> getRecordSetById(@PathVariable Long id) {
        try {
            RecordSet recordSet = recordSetService.getRecordSetById(id);
            return ResponseEntity.ok(recordSet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecordSet(
            @PathVariable Long id,
            @RequestBody AddRecordsRequest request) {
        try {
            RecordSet updated = recordSetService.update(id, request.getRecords());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("기록 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<RecordSet>> getAllRecordSets() {
        List<RecordSet> allRecords = recordSetService.getAllRecordSets();
        return ResponseEntity.ok(allRecords);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecordSet(@PathVariable Long id) {
        try {
            recordSetService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all-filtered-records")
    public ResponseEntity<String> getAllFilteredRecords() {
        try {
            String filteredAll = recordSetService.filterAllRecordSetsWithYakuList();
            return ResponseEntity.ok(filteredAll);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("필터링 중 오류 발생: " + e.getMessage());
        }
    }

    // 닉네임 상세 기록 + 역만 이미지 URL 포함 응답으로 확장
    @GetMapping("/detail")
    public ResponseEntity<?> getNicknameRecords(@RequestParam String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("닉네임은 필수 파라미터입니다.");
        }
        try {
            // 기존 닉네임별 역만 기록 응답
            NicknameDetailResponse response = recordSetService.getRecordsWithCountByNickname(nickname);

            // 닉네임에 연관된 역만 이미지 정보 조회
            List<YakuImage> imageList = yakuImageService.getImagesByNickname(nickname);

            // 역만별 이미지 URL 맵 생성 : yaku -> imageUrl (가장 최근 이미지 기준 등 정책 가능)
            Map<String, String> yakuImageMap = imageList.stream()
                    .collect(Collectors.toMap(
                            YakuImage::getYaku,
                            YakuImage::getImageUrl,
                            (existing, replacement) -> replacement // 중복시 후자를 선택
                    ));

            response.setYakuImageMap(yakuImageMap);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
