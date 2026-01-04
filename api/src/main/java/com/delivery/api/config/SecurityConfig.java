package com.delivery.api.config;

import com.delivery.auth.filter.JwtAuthenticationFilter;
import com.delivery.auth.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 * 
 * 리팩토링 사항:
 * 1. 주석 정리 및 설명 추가
 * 2. Role 불일치 문제 설명 추가
 * 
 * 주의사항:
 * - hasRole()은 자동으로 "ROLE_" 접두사를 추가합니다
 * - 예: hasRole("ADMIN")은 "ROLE_ADMIN"을 찾습니다
 * - UserPrincipal에서 권한을 설정할 때 "ROLE_" 접두사를 포함해야 합니다
 * - 또는 hasAuthority()를 사용하여 접두사 없이 권한을 체크할 수 있습니다
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    /**
     * SecurityFilterChain 빈 설정
     * 
     * 리팩토링: 
     * - 접근 제어는 SecurityConfig에서 수행 (WebConfig는 CORS, 인터셉터 등 설정)
     * - Role 체크 시 "ROLE_" 접두사 자동 추가됨을 주석으로 명시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 공개 엔드포인트: 인증 없이 접근 가능
                        .requestMatchers("/auth/**", "/health", "/actuator/**").permitAll()
                        // 관리자 전용: ROLE_ADMIN 권한 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 점주/관리자: ROLE_OWNER 또는 ROLE_ADMIN 권한 필요
                        .requestMatchers("/api/owner/**").hasAnyRole("OWNER", "ADMIN")
                        // 나머지: 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
