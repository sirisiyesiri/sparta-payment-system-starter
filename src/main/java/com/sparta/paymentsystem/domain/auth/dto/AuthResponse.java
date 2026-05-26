package com.sparta.paymentsystem.domain.auth.dto;

public record AuthResponse(
        String token,
        MemberInfo member
) {
    public record MemberInfo(
            Long id,
            String name,
            String email,
            String phoneNumber
    ) {}
}
