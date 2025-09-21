package me.taeuk.record.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "yaku_images")
public class YakuImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String yaku;

    private String imageUrl;

    private LocalDateTime createdAt;

    public YakuImage() {
    }

    public YakuImage(String nickname, String yaku, String imageUrl) {
        this.nickname = nickname;
        this.yaku = yaku;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }

    // Getter & Setter

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getYaku() {
        return yaku;
    }
    public void setYaku(String yaku) {
        this.yaku = yaku;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
