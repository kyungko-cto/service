package com.delivery.auth.config;

import com.delivery.auth.filter.JwtAuthenticationFilter;
import com.delivery.auth.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    /**
     * 비밀번호 암호화 빈
     *
     * 이유:
     * - BCrypt: 느린 해싱으로 브루트포스 공격 방어
     * - 강도 12: 보안과 성능의 균형 (AWS 권장)
     * - 여러 곳에서 주입받아 사용하므로 빈으로 정의
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 보안 필터 체인 설정
     *
     * 인증 흐름:
     * 1. /auth/** (회원가입, 로그인) → 누구나 접근
     * 2. /api/** → JWT 필수
     * 3. /health, /docs → 공개
     * 4. 나머지 → 인증 필수
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 비활성화: REST API는 CSRF 토큰 불필요
                // 이유: Stateless + JWT 사용
                .csrf(csrf -> csrf.disable())

                // HTTP Basic 인증 비활성화
                // 이유: JWT 기반 인증 사용
                .httpBasic(httpBasic -> httpBasic.disable())

                // 세션 비활성화
                // 이유: Stateless JWT 기반 인증
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                        )
                )

                // 엔드포인트 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 관련 엔드포인트: 모두 허용
                        .requestMatchers("/auth/**").permitAll()

                        // 건강 체크, 문서: 모두 허용
                        .requestMatchers("/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 나머지: 인증 필수
                        .anyRequest().authenticated()
                )

                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 전에)
                // 이유: 요청이 들어오면 가장 먼저 JWT 검증
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}