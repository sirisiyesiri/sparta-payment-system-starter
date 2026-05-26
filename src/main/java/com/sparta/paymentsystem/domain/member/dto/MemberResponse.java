package com.sparta.paymentsystem.domain.member.dto;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        LocalDateTime createdAt
) {}
