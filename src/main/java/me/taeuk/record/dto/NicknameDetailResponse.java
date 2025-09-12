package me.taeuk.record.dto;

import java.util.List;

public class NicknameDetailResponse {
    private long totalCount;  // 닉네임별 총국수
    private List<AddRecordDataRequest> records;  // 닉네임별 상세 기록 리스트

    public NicknameDetailResponse() {}

    public NicknameDetailResponse(long totalCount, List<AddRecordDataRequest> records) {
        this.totalCount = totalCount;
        this.records = records;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<AddRecordDataRequest> getRecords() {
        return records;
    }

    public void setRecords(List<AddRecordDataRequest> records) {
        this.records = records;
    }
}