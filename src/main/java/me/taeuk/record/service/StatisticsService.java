package me.taeuk.record.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.taeuk.record.domain.RecordSet;
import me.taeuk.record.dto.AddRecordDataRequest;
import me.taeuk.record.dto.PlayerStatisticsDto;
import me.taeuk.record.repository.RecordSetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final RecordSetRepository recordSetRepository;
    private final ObjectMapper objectMapper;

    private static final Map<Integer, Integer> RANK_POINT_MAP = Map.of(
            0, 30,
            1, 10,
            2, -10,
            3, -30
    );

    private static final Map<String, Integer> DIRECTION_PRIORITY = Map.of(
            "동", 1,
            "남", 2,
            "서", 3,
            "북", 4
    );

    public StatisticsService(RecordSetRepository recordSetRepository, ObjectMapper objectMapper) {
        this.recordSetRepository = recordSetRepository;
        this.objectMapper = objectMapper;
    }

    // 기존 전체 통계 메서드
    public List<PlayerStatisticsDto> getPlayerStatistics() {
        List<RecordSet> allRecordSets = recordSetRepository.findAllByOrderByIdDesc();
        return calculateStatistics(allRecordSets).stream()
                .sorted(Comparator.comparing(PlayerStatisticsDto::getTotalPoints).reversed()
                        .thenComparing(PlayerStatisticsDto::getNickname))
                .collect(Collectors.toList());
    }

    // 연도와 월 조건으로 필터링해 통계 계산 (월별 통계)
    public List<PlayerStatisticsDto> getStatisticsByYearMonth(int year, int month) {
        List<RecordSet> allRecordSets = recordSetRepository.findAllByOrderByIdDesc();

        List<RecordSet> filtered = allRecordSets.stream()
                .filter(set -> {
                    LocalDateTime dt = set.getTimestamp();
                    return dt.getYear() == year && dt.getMonthValue() == month;
                })
                .collect(Collectors.toList());

        List<PlayerStatisticsDto> stats = calculateStatistics(filtered);
        stats.forEach(s -> s.setPeriodLabel(String.format("%04d-%02d", year, month)));

        return stats;
    }

    // 특정 연도 전체 월에 대한 통계 (1월~12월 합산)
    public List<PlayerStatisticsDto> getMonthlyStatisticsByYear(int year) {
        List<PlayerStatisticsDto> allStats = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            allStats.addAll(getStatisticsByYearMonth(year, month));
        }
        // 정렬: 기간(내림차순), 총승점(내림차순), 닉네임
        allStats.sort(
                Comparator.comparing(PlayerStatisticsDto::getPeriodLabel).reversed()
                        .thenComparing(PlayerStatisticsDto::getTotalPoints).reversed()
                        .thenComparing(PlayerStatisticsDto::getNickname)
        );
        return allStats;
    }

    // 기존 월별 전체 통계 메서드
    public List<PlayerStatisticsDto> getMonthlyStatistics() {
        List<RecordSet> allRecordSets = recordSetRepository.findAllByOrderByIdDesc();

        Map<String, List<RecordSet>> groupedByMonth = new HashMap<>();
        for (RecordSet set : allRecordSets) {
            LocalDateTime dateTime = set.getTimestamp();
            String period = String.format("%04d-%02d", dateTime.getYear(), dateTime.getMonthValue());
            groupedByMonth.computeIfAbsent(period, k -> new ArrayList<>()).add(set);
        }

        Map<String, PlayerStatisticsDto> statsMap = new HashMap<>();
        for (Map.Entry<String, List<RecordSet>> e : groupedByMonth.entrySet()) {
            String period = e.getKey();
            List<PlayerStatisticsDto> stats = calculateStatistics(e.getValue());

            for (PlayerStatisticsDto stat : stats) {
                stat.setPeriodLabel(period);
                String key = stat.getNickname() + "_" + period;
                statsMap.put(key, stat);
            }
        }

        return statsMap.values().stream()
                .sorted(
                        Comparator.comparing(PlayerStatisticsDto::getPeriodLabel).reversed()
                                .thenComparing(PlayerStatisticsDto::getTotalPoints).reversed()
                                .thenComparing(PlayerStatisticsDto::getNickname))
                .collect(Collectors.toList());
    }

    // 기존 연간 통계 메서드
    public List<PlayerStatisticsDto> getYearlyStatistics() {
        List<RecordSet> allRecordSets = recordSetRepository.findAllByOrderByIdDesc();

        Map<String, List<RecordSet>> groupedByYear = new HashMap<>();
        for (RecordSet set : allRecordSets) {
            LocalDateTime dateTime = set.getTimestamp();
            String period = String.format("%04d", dateTime.getYear());
            groupedByYear.computeIfAbsent(period, k -> new ArrayList<>()).add(set);
        }

        Map<String, PlayerStatisticsDto> statsMap = new HashMap<>();
        for (Map.Entry<String, List<RecordSet>> e : groupedByYear.entrySet()) {
            String period = e.getKey();
            List<PlayerStatisticsDto> stats = calculateStatistics(e.getValue());

            for (PlayerStatisticsDto stat : stats) {
                stat.setPeriodLabel(period);
                String key = stat.getNickname() + "_" + period;
                statsMap.put(key, stat);
            }
        }

        return statsMap.values().stream()
                .sorted(
                        Comparator.comparing(PlayerStatisticsDto::getPeriodLabel).reversed()
                                .thenComparing(PlayerStatisticsDto::getTotalPoints).reversed()
                                .thenComparing(PlayerStatisticsDto::getNickname))
                .collect(Collectors.toList());
    }


    // 공통 통계 계산 로직 (RecordSet 리스트 입력 -> PlayerStatisticsDto 리스트 반환)
    private List<PlayerStatisticsDto> calculateStatistics(List<RecordSet> recordSets) {
        Map<String, PlayerStatisticsDto> statsMap = new HashMap<>();

        for (RecordSet set : recordSets) {
            List<AddRecordDataRequest> records;
            try {
                records = objectMapper.readValue(
                        set.getRecords(),
                        new TypeReference<List<AddRecordDataRequest>>() {}
                );
            } catch (Exception e) {
                continue; // 파싱 실패 무시
            }

            // 점수 내림차순 + 점수 동일시 방향 우선순위 오름차순 정렬
            records.sort((a, b) -> {
                int cmpScore = Integer.compare(b.getScore(), a.getScore());
                if (cmpScore != 0) return cmpScore;
                return Integer.compare(
                        DIRECTION_PRIORITY.getOrDefault(a.getDirection(), 100),
                        DIRECTION_PRIORITY.getOrDefault(b.getDirection(), 100)
                );
            });

            for (int rank = 0; rank < records.size(); rank++) {
                AddRecordDataRequest rec = records.get(rank);
                String nickname = rec.getNickname();

                PlayerStatisticsDto stat = statsMap.computeIfAbsent(nickname, PlayerStatisticsDto::new);

                stat.setTotalCount(stat.getTotalCount() + 1);
                stat.setTotalScore(stat.getTotalScore() + rec.getScore());

                int baseScore = rec.getScore();
                int weightedPoint = RANK_POINT_MAP.getOrDefault(rank, 0);
                double point = ((baseScore - 25000) / 1000.0) + weightedPoint;
                stat.setTotalPoints(stat.getTotalPoints() + point);

                switch (rank) {
                    case 0:
                        stat.setFirstPlaceCount(stat.getFirstPlaceCount() + 1);
                        break;
                    case 1:
                        stat.setSecondPlaceCount(stat.getSecondPlaceCount() + 1);
                        break;
                    case 2:
                        stat.setThirdPlaceCount(stat.getThirdPlaceCount() + 1);
                        break;
                    case 3:
                        stat.setFourthPlaceCount(stat.getFourthPlaceCount() + 1);
                        break;
                }
            }
        }

        for (PlayerStatisticsDto stat : statsMap.values()) {
            int n = stat.getTotalCount();
            stat.setAvgScore(n > 0 ? (double) stat.getTotalScore() / n : 0.0);
            stat.setAvgPoints(n > 0 ? stat.getTotalPoints() / n : 0.0);
            stat.setFirstPlaceRate(n > 0 ? (double) stat.getFirstPlaceCount() / n : 0.0);
            stat.setSecondPlaceRate(n > 0 ? (double) stat.getSecondPlaceCount() / n : 0.0);
            stat.setThirdPlaceRate(n > 0 ? (double) stat.getThirdPlaceCount() / n : 0.0);
            stat.setFourthPlaceRate(n > 0 ? (double) stat.getFourthPlaceCount() / n : 0.0);
        }

        return new ArrayList<>(statsMap.values());
    }

}
