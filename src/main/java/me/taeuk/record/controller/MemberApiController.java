package me.taeuk.record.controller;

import me.taeuk.record.domain.Member;
import me.taeuk.record.dto.AddMemberRequest;
import me.taeuk.record.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/addmember")
    public ResponseEntity<?> addMember(@RequestBody @Valid AddMemberRequest request, BindingResult result) {
        // DTO 유효성 검사 결과 확인
        if (result.hasErrors()) {
            String errorMsg = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errorMsg);
        }

        if (memberService.existsByUid(request.getUid())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("이미 존재하는 UID 입니다: " + request.getUid());
        }
        Member savedMember = memberService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMember);
    }

    @PutMapping("/update/{uid}")
    public ResponseEntity<?> updateMember(
            @PathVariable Long uid,
            @RequestBody @Valid AddMemberRequest request,
            BindingResult result) {
        if (result.hasErrors()) {
            String errorMsg = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errorMsg);
        }

        try {
            Member updatedMember = memberService.update(uid, request.getNickname());
            return ResponseEntity.ok(updatedMember);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("존재하지 않는 UID 입니다: " + uid);
        }
    }

    @DeleteMapping("/delete/{uid}")
    public ResponseEntity<?> deleteMember(@PathVariable Long uid) {
        try {
            memberService.deleteByUid(uid);
            return ResponseEntity.ok().body("삭제 성공: UID=" + uid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("존재하지 않는 UID 입니다: " + uid);
        }
    }

    @GetMapping("/nicknames")
    public ResponseEntity<List<String>> getAllNicknames() {
        List<String> nicknames = memberService.findAll()
                .stream()
                .map(Member::getNickname)
                .collect(Collectors.toList());
        return ResponseEntity.ok(nicknames);
    }

    // 추가된 멤버 리스트 조회 API
    @GetMapping("/list")
    public ResponseEntity<List<Member>> getMemberList() {
        List<Member> members = memberService.findAll();
        return ResponseEntity.ok(members);
    }
}