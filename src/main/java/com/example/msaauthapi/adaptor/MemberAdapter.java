package com.example.msaauthapi.adaptor;


import com.example.msaauthapi.dto.MemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value="member-api", path="/members")
public interface MemberAdapter {

    @GetMapping("/auth")
    MemberDto getMember(@RequestParam String loginId);

    @PostMapping("/login")
    ResponseEntity<Void> updateLastLoggedInAt(@RequestHeader("X-USER") long memberId);
}
