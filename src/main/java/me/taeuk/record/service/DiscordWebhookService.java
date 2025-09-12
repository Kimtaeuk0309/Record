package me.taeuk.record.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.taeuk.record.domain.RecordSet;
import me.taeuk.record.dto.AddRecordDataRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DiscordWebhookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 실제 사용하는 웹후크 URL로 변경하세요
    private final String webhookUrl = "https://discord.com/api/webhooks/1410958891575148586/b56xjDKn0ZXTP7JGIbK5OA1YpUGAPRvWiD1mrU8ILXFDU0ldA08ThDBIWdl7P5_V_lVu";

    public void sendRecordSetNotification(RecordSet recordSet) {
        try {
            String recordsJson = recordSet.getRecords();

            // JSON 문자열을 List<AddRecordDataRequest>로 변환
            List<AddRecordDataRequest> records = objectMapper.readValue(recordsJson, new TypeReference<List<AddRecordDataRequest>>(){});

            StringBuilder messageBuilder = new StringBuilder();
            for (AddRecordDataRequest rec : records) {
                messageBuilder.append(rec.getNickname())
                        .append(" ")
                        .append(rec.getScore())
                        .append(" ")
                        .append(rec.getRank())
                        .append("--------------------------");
            }

            Map<String, String> payload = Map.of("content", messageBuilder.toString());
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            restTemplate.postForObject(webhookUrl, request, String.class);

        } catch (Exception e) {
            e.printStackTrace();
            // 필요 시 로깅이나 재시도 구현 가능
        }
    }
}
