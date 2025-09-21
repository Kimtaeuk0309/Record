package me.taeuk.record.dto;

import java.util.List;
import java.util.Map;

public class NicknameDetailResponse {
    private long totalCount;  // 닉네임별 총국수
    private List<AddRecordDataRequest> records;  // 닉네임별 상세 기록 리스트

    // 역만별 이미지 URL 매핑: yaku 이름 -> 이미지 URL
    private Map<String, String> yakuImageMap;

    public NicknameDetailResponse() {}

    public NicknameDetailResponse(long totalCount, List<AddRecordDataRequest> records, Map<String, String> yakuImageMap) {
        this.totalCount = totalCount;
        this.records = records;
        this.yakuImageMap = yakuImageMap;
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

    public Map<String, String> getYakuImageMap() {
        return yakuImageMap;
    }

    public void setYakuImageMap(Map<String, String> yakuImageMap) {
        this.yakuImageMap = yakuImageMap;
    }
}
