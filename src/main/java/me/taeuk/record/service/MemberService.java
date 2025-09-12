package me.taeuk.record.service;

import me.taeuk.record.domain.Member;
import me.taeuk.record.dto.AddMemberRequest;
import me.taeuk.record.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public boolean existsByUid(Long uid){
        return memberRepository.existsByUid(uid);
    }

    public Member save(AddMemberRequest request) {
        return memberRepository.save(request.toEntity());
    }

    public Member findByUid(Long uid) {
        return memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 UID 입니다."));
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 대소문자 구분 없는 닉네임 조회 메서드 추가
    public Optional<Member> findByNicknameIgnoreCase(String nickname) {
        return memberRepository.findByNicknameIgnoreCase(nickname);
    }

    // 기존 findByNickname 은 내부에서 대소문자 무시 메서드를 활용
    public Member findByNickname(String nickname) {
        return findByNicknameIgnoreCase(nickname).orElse(null);
    }
}
