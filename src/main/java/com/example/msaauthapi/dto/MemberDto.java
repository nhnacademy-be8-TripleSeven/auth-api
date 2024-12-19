package com.example.msaauthapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private Long id;
    private MemberAccount memberAccount;
    private List<String> roles;

    @Getter
    @AllArgsConstructor
    public static class MemberAccount {
        private String id;
        private String password;
    }
}
