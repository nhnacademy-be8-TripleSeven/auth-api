package com.example.msaauthapi.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MemberLoginRequest {

    private String loginId;
    private String password;
}
