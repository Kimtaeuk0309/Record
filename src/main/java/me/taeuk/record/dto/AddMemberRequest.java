package me.taeuk.record.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taeuk.record.domain.Member;

@NoArgsConstructor
@AllArgsConstructor
@Getter

public class AddMemberRequest {

    @NotNull(message = "UID는 필수입니다.")
    private Long uid;

    @NotNull(message = "닉네임은 필수입니다.")
    private String nickname;

    public Member toEntity() {
        return Member.builder()
                .uid(uid)
                .nickname(nickname)
                .build();
    }

}
