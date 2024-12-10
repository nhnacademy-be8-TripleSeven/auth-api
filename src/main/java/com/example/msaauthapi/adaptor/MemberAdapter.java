package com.example.msaauthapi.adaptor;


import com.example.msaauthapi.dto.MemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value="member-api", path="/members")
public interface MemberAdapter {

    @GetMapping
    MemberDto getMember(@RequestParam String id);

}
