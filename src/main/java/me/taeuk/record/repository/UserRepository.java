package me.taeuk.record.repository;

import me.taeuk.record.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // 추가 사용자 조회 메서드 필요 시 선언 가능
}