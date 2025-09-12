package me.taeuk.record.service;

import me.taeuk.record.domain.RecordData;
import me.taeuk.record.dto.AddRecordDataRequest;
import me.taeuk.record.repository.RecordDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordDataService {

    private final RecordDataRepository recordDataRepository;
    private final MemberService memberService;

    public RecordDataService(RecordDataRepository recordDataRepository, MemberService memberService) {
        this.recordDataRepository = recordDataRepository;
        this.memberService = memberService;
    }

    public List<RecordData> saveAll(List<AddRecordDataRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("기록 데이터가 존재하지 않습니다.");
        }

        List<RecordData> records = requests.stream().map(request -> {
            var member = memberService.findByNickname(request.getNickname());
            if (member == null) {
                throw new IllegalArgumentException("존재하지 않는 닉네임입니다: " + request.getNickname());
            }
            return RecordData.builder()
                    .uid(member.getUid())
                    .nickname(member.getNickname())
                    .score(request.getScore())
                    .direction(request.getDirection())
                    .build();
        }).collect(Collectors.toList());

        return recordDataRepository.saveAll(records);
    }
}
