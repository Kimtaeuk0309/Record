package me.taeuk.record.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class YakuImageDto {
    private String yaku;
    private String imageUrl;

    public YakuImageDto(String yaku, String imageUrl) {
        this.yaku = yaku;
        this.imageUrl = imageUrl;
    }

    // getter, setter 생략 가능 (Lombok 사용 시 @Data 등 활용)
    public String getYaku() { return yaku; }
    public void setYaku(String yaku) { this.yaku = yaku; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
