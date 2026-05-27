package com.sparta.paymentsystem.domain.auth.service;

import com.sparta.paymentsystem.global.error.BusinessException;
import com.sparta.paymentsystem.global.error.ErrorCode;
import com.sparta.paymentsystem.global.jwt.JwtProvider;
import com.sparta.paymentsystem.domain.member.entity.Member;
import com.sparta.paymentsystem.domain.auth.dto.AuthResponse;
import com.sparta.paymentsystem.domain.auth.dto.LoginRequest;
import com.sparta.paymentsystem.domain.auth.dto.SignupRequest;
import com.sparta.paymentsystem.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = new Member(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.phoneNumber()
        );
        memberRepository.save(member);
    }

    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String token = jwtProvider.createToken(member.getId(), member.getEmail());
        return new AuthResponse(token, toMemberInfo(member));
    }

    private AuthResponse.MemberInfo toMemberInfo(Member member) {
        return new AuthResponse.MemberInfo(member.getId(), member.getName(), member.getEmail(), member.getPhoneNumber());
    }
}