package me.taeuk.record.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long uid;

    private String nickname;

    private Integer score;

    private String direction;

    @Builder
    public RecordData(Long uid, String nickname, Integer score, String direction) {
        this.uid = uid;
        this.nickname = nickname;
        this.score = score;
        this.direction = direction;
    }
}
