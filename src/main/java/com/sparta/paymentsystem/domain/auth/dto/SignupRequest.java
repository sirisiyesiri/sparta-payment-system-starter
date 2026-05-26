package com.sparta.paymentsystem.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이름을 입력하세요")
        @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다")
        String name,

        @NotBlank(message = "이메일을 입력하세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호를 입력하세요")
        @Size(min = 4, max = 100, message = "비밀번호는 4자 이상이어야 합니다")
        String password,

        @NotBlank(message = "전화번호를 입력하세요")
        @Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
        String phoneNumber
) {}
