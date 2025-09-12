package me.taeuk.record.controller;

import me.taeuk.record.domain.RecordData;
import me.taeuk.record.dto.AddRecordsRequest;
import me.taeuk.record.service.RecordDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordDataApiController {

    private final RecordDataService recordDataService;

    public RecordDataApiController(RecordDataService recordDataService) {
        this.recordDataService = recordDataService;
    }

    @PostMapping
    public ResponseEntity<List<RecordData>> createRecords(@RequestBody AddRecordsRequest request) {
        List<RecordData> savedRecords = recordDataService.saveAll(request.getRecords());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecords);
    }
}
