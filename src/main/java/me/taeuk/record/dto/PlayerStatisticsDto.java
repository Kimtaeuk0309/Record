package me.taeuk.record.dto;

import lombok.Data;

@Data
public class PlayerStatisticsDto {
    private String nickname;
    private int totalCount;
    private int totalScore;
    private double avgScore;
    private int firstPlaceCount;
    private int secondPlaceCount;
    private int thirdPlaceCount;
    private int fourthPlaceCount;
    private double firstPlaceRate;
    private double secondPlaceRate;
    private double thirdPlaceRate;
    private double fourthPlaceRate;
    private double totalPoints;
    private double avgPoints;

    // 기간별 라벨 (예: "2025-09" 또는 "2025")
    private String periodLabel;

    public PlayerStatisticsDto() {}

    public PlayerStatisticsDto(String nickname) {
        this.nickname = nickname;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public double getAvgPoints() {
        return avgPoints;
    }

    public void setAvgPoints(double avgPoints) {
        this.avgPoints = avgPoints;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }
}