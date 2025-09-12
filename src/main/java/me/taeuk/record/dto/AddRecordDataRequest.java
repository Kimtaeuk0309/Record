package me.taeuk.record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taeuk.record.domain.RecordData;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddRecordDataRequest {

    private Long uid;
    private String nickname;
    private Integer score;
    private String direction;

    // 새로 추가한 순위 필드 (null 가능)
    private Integer rank;

    // ★ 역만 리스트
    private List<String> yakuList;

    // ★ 기록 생성 날짜 필드 추가
    private LocalDateTime timestamp;

    public RecordData toEntity() {
        return RecordData.builder()
                .uid(uid)
                .nickname(nickname)
                .score(score)
                .direction(direction)
                // yakuList 엔티티 반영 시 아래에 추가
                .build();
    }
}
