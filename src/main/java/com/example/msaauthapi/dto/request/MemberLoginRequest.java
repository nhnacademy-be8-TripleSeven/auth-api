package com.example.msaauthapi.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MemberLoginRequest {

    @NotNull
    private String loginId;
    @NotNull
    private String password;
}
