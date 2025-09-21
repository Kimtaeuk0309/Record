package me.taeuk.record.repository;

import me.taeuk.record.domain.YakuImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YakuImageRepository extends JpaRepository<YakuImage, Long> {

    List<YakuImage> findByNickname(String nickname);

    List<YakuImage> findByNicknameAndYaku(String nickname, String yaku);

    void deleteByNicknameAndYaku(String nickname, String yaku);
}
