package me.taeuk.record.controller;

import me.taeuk.record.service.StatisticsService;
import me.taeuk.record.dto.PlayerStatisticsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public List<PlayerStatisticsDto> getPlayerStatistics() {
        return statisticsService.getPlayerStatistics();
    }

    @GetMapping("/period")
    public ResponseEntity<List<PlayerStatisticsDto>> getPeriodStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String month
    ) {
        if (year == null) {
            // year 미지정 시 전체 데이터 반환 (기존 전체 통계 재사용)
            return ResponseEntity.ok(statisticsService.getPlayerStatistics());
        }

        List<PlayerStatisticsDto> stats;
        if (month == null || month.equalsIgnoreCase("all")) {
            // 해당 년도 전체 월 데이터
            stats = statisticsService.getMonthlyStatisticsByYear(year);
        } else {
            try {
                int monthInt = Integer.parseInt(month);
                if (monthInt < 1 || monthInt > 12) {
                    return ResponseEntity.badRequest().build();
                }
                // 해당 년도, 해당 월 데이터
                stats = statisticsService.getStatisticsByYearMonth(year, monthInt);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(stats);
    }
}
