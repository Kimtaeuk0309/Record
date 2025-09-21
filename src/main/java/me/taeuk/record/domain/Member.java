package me.taeuk.record.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "uid", nullable = false)
    private Long uid;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Builder
    public Member(Long uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }

    // 닉네임 변경용 메서드 추가
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }
}
