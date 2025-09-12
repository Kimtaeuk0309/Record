package me.taeuk.record.repository;

import me.taeuk.record.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUid(Long uid);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findByNicknameIgnoreCase(String nickname);
}
