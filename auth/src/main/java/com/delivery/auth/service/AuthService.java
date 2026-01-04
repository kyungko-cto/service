package com.delivery.auth.service;

import com.delivery.auth.converter.UserConverter;
import com.delivery.auth.dto.*;
import com.delivery.auth.provider.JwtProvider;
import com.delivery.common.exception.DuplicateEmailException;
import com.delivery.common.exception.InvalidCredentialsException;
import com.delivery.common.exception.JwtException;
import com.delivery.db.entity.user.UserEntity;
import com.delivery.db.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/**
 * 인증 서비스
 *
 * 책임:
 * 1. 회원가입 (이메일 중복 확인, 비밀번호 암호화)
 * 2. 로그인 (자격 증명 검증, 토큰 발급)
 * 3. 토큰 갱신 (RefreshToken → AccessToken)
 * 4. 회원 탈퇴 (DB 삭제, 토큰 폐기)
 *
 * 데이터 스토어:
 * - DB (UserRepository): 사용자 정보, 영구 저장
 * - Redis (StringRedisTemplate): RefreshToken, TTL 자동 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    // Redis 키 접두사
    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    // RefreshToken 만료 시간 (7일)
    private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(7);

    /**
     * 회원가입
     *
     * 비즈니스 로직:
     * 1. 이메일 중복 확인
     * 2. 비밀번호 암호화 (BCrypt)
     * 3. 사용자 생성 및 저장
     *
     * 트랜잭션: DB에 사용자 저장이 완료될 때까지 보장
     *
     * @param request 회원가입 요청
     * @return 생성된 사용자 정보
     * @throws DuplicateEmailException 이미 가입한 이메일
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("중복된 이메일로 회원가입 시도: {}", request.getEmail());
            throw new DuplicateEmailException("이미 가입된 이메일입니다");
        }

        // 2. 비밀번호 암호화 (BCrypt)
        // 이유: 평문으로 저장하면 안 됨
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 사용자 엔티티 생성
        // Converter가 기본값 설정 (role: ROLE_USER, grade: BRONZE)
        UserEntity user = UserConverter.toEntity(request, encodedPassword);

        // 4. DB에 저장
        UserEntity savedUser = userRepository.save(user);

        log.info("새 사용자 가입: {}", savedUser.getEmail());

        // 5. 응답 DTO로 변환하여 반환
        return UserConverter.toResponse(savedUser);
    }

    /**
     * 로그인
     *
     * 비즈니스 로직:
     * 1. 이메일로 사용자 조회
     * 2. 비밀번호 검증 (BCrypt 비교)
     * 3. AccessToken + RefreshToken 발급
     * 4. RefreshToken을 Redis에 저장 (TTL 7일)
     *
     * Redis 저장 이유:
     * - 로그아웃 시 즉시 토큰 폐기 가능
     * - DB 조회 오버헤드 제거
     * - TTL 자동 삭제로 메모리 효율
     *
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return AccessToken + RefreshToken
     * @throws InvalidCredentialsException 이메일 없음 또는 비밀번호 틀림
     */
    public TokenResponse login(String email, String password) {
        // 1. 이메일로 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도: {}", email);
                    return new InvalidCredentialsException("이메일 또는 비밀번호가 틀렸습니다");
                });

        // 2. 비밀번호 검증 (BCrypt)
        // matches(입력한 비밀번호, DB의 암호화된 비밀번호)
        // 리팩토링: getPassword() -> getPasswordHash() (UserEntity 필드명 변경)
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("잘못된 비밀번호로 로그인 시도: {}", email);
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 틀렸습니다");
        }

        // 3. 사용자 정보를 UserRequest(JWT 발급용)로 변환
        UserRequest userRequest = UserConverter.toRequest(user);

        // 4. AccessToken 생성 (유효기간: 15분)
        String accessToken = jwtProvider.generateAccessToken(userRequest);

        // 5. RefreshToken 생성 (유효기간: 7일)
        String refreshToken = jwtProvider.generateRefreshToken(userRequest);

        // 6. RefreshToken을 Redis에 저장
        // 키: "RT:{userId}"
        // 값: RefreshToken
        // TTL: 7일 (만료 시간과 동일)
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, REFRESH_TOKEN_EXPIRY);

        log.info("사용자 로그인: {}", email);

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * AccessToken 갱신
     *
     * 비즈니스 로직:
     * 1. RefreshToken 검증
     * 2. RefreshToken에서 이메일 추출
     * 3. 이메일로 사용자 조회
     * 4. Redis에 저장된 RefreshToken 검증 (선택: 보안 강화)
     * 5. 새로운 AccessToken + RefreshToken 발급
     *
     * RefreshToken 검증 이유:
     * - 로그아웃 후 갱신 방지
     * - 토큰 탈취 시 즉시 폐기 가능
     *
     * @param refreshToken 클라이언트에서 제공한 RefreshToken (쿠키)
     * @return 새로운 AccessToken + RefreshToken
     * @throws InvalidCredentialsException 사용자 없음
     * @throws JwtException RefreshToken 검증 실패
     */
    public TokenResponse refreshByToken(String refreshToken) {
        // 1. RefreshToken 검증 (서명, 만료 시간)
        try {
            jwtProvider.validateToken(refreshToken);
        } catch (JwtException e) {
            log.warn("유효하지 않은 RefreshToken: {}", e.getMessage());
            throw new JwtException("RefreshToken이 만료되었습니다. 다시 로그인하세요");
        }

        // 2. RefreshToken에서 이메일 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);

        // 3. 이메일로 사용자 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("조회되지 않는 사용자의 RefreshToken 사용: {}", email);
                    return new InvalidCredentialsException("사용자를 찾을 수 없습니다");
                });

        // 4. Redis에 저장된 RefreshToken과 비교 (선택: 보안 강화)
        // 목적: 로그아웃한 사용자도 갱신 방지
        String storedRefreshToken = redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_PREFIX + user.getId());

        if (!refreshToken.equals(storedRefreshToken)) {
            log.warn("Redis의 RefreshToken과 불일치: {}", email);
            throw new JwtException("RefreshToken이 유효하지 않습니다");
        }

        // 5. 새로운 토큰 발급
        UserRequest userRequest = UserConverter.toRequest(user);
        String newAccessToken = jwtProvider.generateAccessToken(userRequest);
        String newRefreshToken = jwtProvider.generateRefreshToken(userRequest);

        // 6. Redis의 RefreshToken 갱신 (TTL 연장)
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, newRefreshToken, REFRESH_TOKEN_EXPIRY);

        log.info("토큰 갱신: {}", email);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 회원 탈퇴
     *
     * 비즈니스 로직:
     * 1. DB에서 사용자 삭제
     * 2. Redis의 RefreshToken 삭제 (즉시 로그아웃)
     *
     * 트랜잭션: DB 삭제와 Redis 삭제 모두 성공해야 함
     *
     * @param userId 삭제할 사용자 ID (UUID)
     * 
     * 리팩토링 완료:
     * - UserPrincipal의 id 타입을 UUID로 변경하여 타입 일관성 확보
     */
    @Transactional
    public void deleteUser(UUID userId) {
        // 1. DB에서 사용자 삭제
        userRepository.deleteById(userId);

        // 2. Redis의 RefreshToken 삭제 (즉시 로그아웃)
        // 이유: 탈퇴 후 남은 토큰으로 접근 방지
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(refreshTokenKey);

        if (deleted != null && deleted) {
            log.info("사용자 토큰 삭제: {}", userId);
        }

        log.info("사용자 탈퇴: {}", userId);
    }

    /**
     * RefreshToken으로 로그아웃 (토큰 폐기)
     *
     * 구현 예시 (필요 시 추가):
     * 사용자가 명시적으로 로그아웃할 때 RefreshToken 삭제
     *
     * @param userId 로그아웃할 사용자 ID
     */
    public void logout(Long userId) {
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(refreshTokenKey);
        log.info("사용자 로그아웃: {}", userId);
    }
}