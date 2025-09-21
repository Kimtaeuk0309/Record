package me.taeuk.record.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.taeuk.record.domain.Member;
import me.taeuk.record.domain.RecordSet;
import me.taeuk.record.dto.AddRecordDataRequest;
import me.taeuk.record.dto.NicknameDetailResponse;
import me.taeuk.record.domain.YakuImage;
import me.taeuk.record.repository.RecordSetRepository;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecordSetService {

    private final RecordSetRepository recordSetRepository;
    private final ObjectMapper objectMapper;
    private final MemberService memberService;
    private final YakuImageService yakuImageService;

    private static final String DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1410958891575148586/b56xjDKn0ZXTP7JGIbK5OA1YpUGAPRvWiD1mrU8ILXFDU0ldA08ThDBIWdl7P5_V_lVu";
    private static final RestTemplate restTemplate = new RestTemplate();

    public RecordSetService(RecordSetRepository recordSetRepository,
                            ObjectMapper objectMapper,
                            MemberService memberService,
                            YakuImageService yakuImageService) {
        this.recordSetRepository = recordSetRepository;
        this.objectMapper = objectMapper;
        this.memberService = memberService;
        this.yakuImageService = yakuImageService;
    }


    // 새 기록 저장 시 현재 시간으로 타임스탬프 자동 입력
    public RecordSet save(List<AddRecordDataRequest> requests) {
        try {
            List<AddRecordDataRequest> filledRequests = fillUidAndNormalizeNicknameForRequests(requests);
            String json = objectMapper.writeValueAsString(filledRequests);

            RecordSet recordSet = RecordSet.builder()
                    .records(json)
                    .timestamp(LocalDateTime.now())
                    .build();

            RecordSet saved = recordSetRepository.save(recordSet);

            sendDiscordWebhookNotification(filledRequests);

            return saved;
        } catch (Exception e) {
            throw new RuntimeException("기록 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 기존 타임스탬프 유지하며 기록 업데이트
    public RecordSet update(Long id, List<AddRecordDataRequest> requests) {
        try {
            RecordSet existing = recordSetRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("수정 대상 기록이 없습니다. id=" + id));
            List<AddRecordDataRequest> filledRequests = fillUidAndNormalizeNicknameForRequests(requests);
            String json = objectMapper.writeValueAsString(filledRequests);

            existing.setRecords(json);
            // 타임스탬프는 수정하지 않고 그대로 유지
            RecordSet saved = recordSetRepository.save(existing);

            // 필요 시 웹훅 알림 재전송
            // sendDiscordWebhookNotification(filledRequests);

            return saved;
        } catch (Exception e) {
            throw new RuntimeException("기록 수정 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private List<AddRecordDataRequest> fillUidAndNormalizeNicknameForRequests(List<AddRecordDataRequest> requests) {
        return requests.stream()
                .map(req -> {
                    Member member = memberService.findByNicknameIgnoreCase(req.getNickname())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임: " + req.getNickname()));

                    req.setNickname(member.getNickname());
                    req.setUid(member.getUid());
                    return req;
                })
                .collect(Collectors.toList());
    }

    private void sendDiscordWebhookNotification(List<AddRecordDataRequest> records) {
        try {
            List<AddRecordDataRequest> ranked = assignRanks(records);

            StringBuilder message = new StringBuilder();
            for (AddRecordDataRequest r : ranked) {
                message.append(String.format("%-7s %7d점 %4d위\n", r.getNickname(), r.getScore(), r.getRank()));
            }

            JSONObject json = new JSONObject();
            json.put("content", "```\n" + message.toString() + "```");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);
            restTemplate.postForObject(DISCORD_WEBHOOK_URL, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AddRecordDataRequest> getRecordsFromRecordSet(RecordSet recordSet) {
        try {
            return objectMapper.readValue(recordSet.getRecords(), new TypeReference<List<AddRecordDataRequest>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("기록 파싱 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public RecordSet getLatestRecordSet() {
        return recordSetRepository.findTopRecordSet()
                .orElseThrow(() -> new RuntimeException("저장된 기록이 없습니다."));
    }

    public List<RecordSet> getAllRecordSets() {
        return recordSetRepository.findAllByOrderByIdDesc();
    }

    public RecordSet getRecordSetById(Long id) {
        return recordSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 기록입니다. id=" + id));
    }

    public void deleteById(Long id) {
        if (!recordSetRepository.existsById(id)) {
            throw new RuntimeException("존재하지 않는 기록 id=" + id);
        }
        recordSetRepository.deleteById(id);
    }

    public List<AddRecordDataRequest> getRecordsByNicknameWithRank(String nickname) {
        List<RecordSet> allRecordSets = getAllRecordSets();
        List<AddRecordDataRequest> result = new ArrayList<>();

        for (RecordSet set : allRecordSets) {
            List<AddRecordDataRequest> records = getRecordsFromRecordSet(set);
            List<AddRecordDataRequest> ranked = assignRanks(records);

            List<AddRecordDataRequest> filtered = ranked.stream()
                    .filter(r -> r.getNickname().equals(nickname))
                    .collect(Collectors.toList());

            Set<String> positionsPresent = filtered.stream()
                    .map(AddRecordDataRequest::getDirection)
                    .collect(Collectors.toSet());

            for (String pos : List.of("동", "남", "서", "북")) {
                if (!positionsPresent.contains(pos)) {
                    AddRecordDataRequest emptyRec = new AddRecordDataRequest();
                    emptyRec.setNickname(nickname);
                    emptyRec.setDirection(pos);
                    emptyRec.setScore(0);
                    emptyRec.setRank(0);
                    emptyRec.setUid(null);
                    filtered.add(emptyRec);
                }
            }

            for (AddRecordDataRequest rec : filtered) {
                rec.setTimestamp(set.getTimestamp());
            }

            result.addAll(filtered);
        }

        return result;
    }

    public NicknameDetailResponse getRecordsWithCountByNickname(String nickname) {
        List<RecordSet> allRecordSets = getAllRecordSets();
        List<AddRecordDataRequest> result = new ArrayList<>();

        for (RecordSet set : allRecordSets) {
            List<AddRecordDataRequest> records = getRecordsFromRecordSet(set);
            List<AddRecordDataRequest> ranked = assignRanks(records);

            List<AddRecordDataRequest> filtered = ranked.stream()
                    .filter(r -> r.getNickname().equals(nickname))
                    .collect(Collectors.toList());

            Set<String> positionsPresent = filtered.stream()
                    .map(AddRecordDataRequest::getDirection)
                    .collect(Collectors.toSet());

            for (String pos : List.of("동", "남", "서", "북")) {
                if (!positionsPresent.contains(pos)) {
                    AddRecordDataRequest emptyRec = new AddRecordDataRequest();
                    emptyRec.setNickname(nickname);
                    emptyRec.setDirection(pos);
                    emptyRec.setScore(0);
                    emptyRec.setRank(0);
                    emptyRec.setUid(null);
                    filtered.add(emptyRec);
                }
            }

            for (AddRecordDataRequest rec : filtered) {
                rec.setTimestamp(set.getTimestamp());
            }

            result.addAll(filtered);
        }

        long totalCount = result.stream()
                .filter(r -> r.getScore() != 0)
                .count();

        List<YakuImage> imageList = yakuImageService.getImagesByNickname(nickname);

        Map<String, String> yakuImageMap = imageList.stream()
                .collect(Collectors.toMap(
                        YakuImage::getYaku,
                        YakuImage::getImageUrl,
                        (existing, replacement) -> replacement // 중복시 후자 선택
                ));

        NicknameDetailResponse response = new NicknameDetailResponse();
        response.setTotalCount(totalCount);
        response.setRecords(result);
        response.setYakuImageMap(yakuImageMap);

        return response;
    }

    private List<AddRecordDataRequest> assignRanks(List<AddRecordDataRequest> records) {
        List<AddRecordDataRequest> sorted = new ArrayList<>(records);

        Map<String, Integer> directionOrder = Map.of(
                "동", 1,
                "남", 2,
                "서", 3,
                "북", 4
        );

        sorted.sort(Comparator.comparingInt(AddRecordDataRequest::getScore).reversed()
                .thenComparing(r -> directionOrder.getOrDefault(r.getDirection(), 99)));

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setRank(i + 1);
        }

        return sorted;
    }

    public String filterAllRecordSetsWithYakuList() throws Exception {
        List<RecordSet> allRecordSets = recordSetRepository.findAll();

        List<PlayerRecord> allFilteredRecords = new ArrayList<>();

        for (RecordSet recordSet : allRecordSets) {
            List<PlayerRecord> filtered = filterRecordsWithYakuListAsList(recordSet.getRecords(), recordSet.getTimestamp());
            allFilteredRecords.addAll(filtered);
        }

        return objectMapper.writeValueAsString(allFilteredRecords);
    }

    public List<PlayerRecord> filterRecordsWithYakuListAsList(String recordsJson, LocalDateTime recordSetTimestamp) throws Exception {
        if (recordsJson == null || recordsJson.isBlank()) {
            return Collections.emptyList();
        }
        List<PlayerRecord> records = objectMapper.readValue(recordsJson, new TypeReference<List<PlayerRecord>>() {});
        return records.stream()
                .filter(r -> r.getYakuList() != null && !r.getYakuList().isEmpty())
                .peek(r -> r.setTimestamp(recordSetTimestamp))
                .collect(Collectors.toList());
    }

    // PlayerRecord DTO (내부클래스)
    public static class PlayerRecord {
        private String nickname;
        private int score;
        private String direction;
        private List<String> yakuList;
        private LocalDateTime timestamp;

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public PlayerRecord() {
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public List<String> getYakuList() {
            return yakuList;
        }

        public void setYakuList(List<String> yakuList) {
            this.yakuList = yakuList;
        }
    }
}
