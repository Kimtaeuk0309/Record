package me.taeuk.record.controller;

import me.taeuk.record.domain.RecordSet;
import me.taeuk.record.dto.AddRecordsRequest;
import me.taeuk.record.dto.NicknameDetailResponse;
import me.taeuk.record.service.RecordSetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordset")
public class RecordSetApiController {

    private final RecordSetService recordSetService;

    public RecordSetApiController(RecordSetService recordSetService) {
        this.recordSetService = recordSetService;
    }

    @PostMapping
    public ResponseEntity<?> createRecordSet(@RequestBody AddRecordsRequest request) {
        try {
            RecordSet saved = recordSetService.save(request.getRecords());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            // 닉네임 없음 등 검증 실패 시 400 Bad Request
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

    @GetMapping("/detail")
    public ResponseEntity<?> getNicknameRecords(@RequestParam String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("닉네임은 필수 파라미터입니다.");
        }
        try {
            // 대소문자 구분 없이 닉네임으로 조회
            NicknameDetailResponse response = recordSetService.getRecordsWithCountByNickname(nickname);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
