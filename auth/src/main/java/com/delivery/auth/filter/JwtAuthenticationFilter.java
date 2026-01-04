package com.delivery.auth.filter;

import com.delivery.auth.model.UserPrincipal;
import com.delivery.auth.provider.JwtProvider;
import com.delivery.common.exception.JwtException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT 인증 필터
 *
 * 역할:
 * 1. 요청 헤더에서 JWT 토큰 추출
 * 2. 토큰 검증 (유효성, 만료 시간)
 * 3. 토큰에서 사용자 정보 추출
 * 4. SecurityContext에 인증 정보 설정
 * 5. 인증 실패 시 요청 계속 진행 (ExceptionHandler가 처리)
 *
 * 호출 순서:
 * 요청 → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → 컨트롤러
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // 상수: Authorization 헤더 접두사
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * 필터 실행 로직
     *
     * 동작:
     * 1. Authorization 헤더 추출
     * 2. "Bearer " 접두사 확인
     * 3. 토큰 검증
     * 4. 토큰에서 사용자 정보 추출
     * 5. SecurityContext에 설정
     * 6. 예외 발생 시 SecurityContext 초기화 (인증 불가 상태)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Authorization 헤더 추출
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            // 2. "Bearer " 접두사 확인
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                // JWT 없음 → 필터는 통과 (public 엔드포인트일 수 있음)
                // 권한 필요 시 SecurityFilterChain에서 거절
                filterChain.doFilter(request, response);
                return;
            }

            // 3. "Bearer " 제거하고 순수 토큰 추출
            String token = authHeader.substring(BEARER_PREFIX_LENGTH);

            if (token.isEmpty()) {
                log.warn("빈 JWT 토큰이 전송됨");
                filterChain.doFilter(request, response);
                return;
            }

            // 4. 토큰 유효성 검증 (서명, 만료 시간 등)
            jwtProvider.validateToken(token);

            // 5. 토큰에서 클레임(사용자 정보) 추출
            Claims claims = jwtProvider.getClaims(token);

            // 6. 클레임에서 사용자 정보 추출
            // 리팩토링: UserPrincipal이 UUID를 사용하므로 UUID로 변환
            UUID userId = jwtProvider.getUserIdFromToken(token);
            String email = claims.getSubject();  // sub 클레임: 이메일
            String role = String.valueOf(claims.get("role"));
            String grade = String.valueOf(claims.get("grade"));

            // 7. UserPrincipal 생성 (Spring Security 사용자 객체)
            UserPrincipal principal = new UserPrincipal(userId, email, role, grade);

            // 8. 인증 토큰 생성
            // 생성자: (principal, credentials, authorities)
            // principal: 사용자 정보
            // credentials: null (이미 검증됨, JWT는 비밀번호 불필요)
            // authorities: 권한 정보
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            // 9. SecurityContext에 인증 정보 설정
            // 이후 @AuthenticationPrincipal로 접근 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 인증 성공 - 사용자: {}", email);

        } catch (JwtException e) {
            // JWT 검증 실패 (만료, 변조, 형식 오류 등)
            // SecurityContext를 초기화하여 인증되지 않은 상태로 설정
            log.warn("JWT 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            // 예상치 못한 예외 (로직 오류, null pointer 등)
            log.error("JWT 처리 중 예상치 못한 오류 발생", e);
            SecurityContextHolder.clearContext();
        }

        // 다음 필터로 진행 (항상 실행)
        // JWT 없거나 검증 실패해도 필터는 통과 → 권한 필터에서 거절
        filterChain.doFilter(request, response);
    }
}