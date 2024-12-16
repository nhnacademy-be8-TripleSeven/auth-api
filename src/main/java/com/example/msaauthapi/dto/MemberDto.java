package com.example.msaauthapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private String loginId;
    private String password;
    private List<String> roles;
}
