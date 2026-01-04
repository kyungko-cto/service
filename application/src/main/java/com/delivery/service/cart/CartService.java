package com.delivery.service.cart;

import com.delivery.domain.cart.Cart;
import com.delivery.domain.cart.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 장바구니 서비스
 *
 * 책임:
 * 1. 사용자별 장바구니 관리 (Redis 캐시 활용)
 * 2. 아이템 추가/제거/조회 비즈니스 로직
 * 3. 1시간 미사용 시 자동 삭제 (메모리 절약)
 * 
 * 리팩토링 사항:
 * - 타입 불일치 문제: Long userId 사용 (UserEntity는 UUID 사용)
 * - TODO: 일관성을 위해 UUID로 변경 고려 필요
 * - 현재는 UserPrincipal이 Long id를 사용하므로 Long 유지
 * 
 * 설계 원칙:
 * - application 모듈의 서비스는 도메인별로 분리
 *   이유: 단일 책임 원칙 준수, 유지보수성 향상
 * - Redis 캐시 활용: 세션 기반 장바구니 관리
 * - 도메인 객체 활용: Cart, CartItem 도메인 객체로 비즈니스 로직 캡슐화
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 상수: Redis 키 생성 규칙
    private static final String CART_KEY_PREFIX = "cart:";

    // 상수: 장바구니 만료 시간 (1시간)
    // 변경 시: 이 상수 한 곳만 수정하면 모든 메서드에 적용됨
    private static final Duration CART_EXPIRY = Duration.ofHours(1);

    /**
     * 장바구니에 상품 추가
     *
     * 비즈니스 로직:
     * 1. Redis에서 기존 카트 조회
     * 2. 없으면 새 카트 생성
     * 3. 상품 추가
     * 4. Redis에 저장 (TTL 갱신)
     *
     * @param userId 사용자 ID
     * @param item 추가할 상품 정보
     */
    public void addItem(Long userId, CartItem item) {
        // 상품 유효성 검증 (null 체크 + 값 검증)
        if (item == null) {
            throw new IllegalArgumentException("상품 정보는 필수입니다");
        }
        item.validate();

        // Redis 키 생성
        String key = buildCartKey(userId);

        // 1. Redis에서 기존 카트 조회
        Object cached = redisTemplate.opsForValue().get(key);
        Cart cart = null;

        // 2. instanceof 체크로 안전하게 타입 변환
        if (cached instanceof Cart) {
            cart = (Cart) cached;
        } else {
            // 캐시가 없거나 유효하지 않으면 새 카트 생성
            cart = createNewCart(userId);
        }

        // 3. 같은 가게 상품인지 검증 (배달의민족: 한 가게만 주문 가능)
        if (!cart.isSameStore(item.getStoreId())) {
            throw new IllegalArgumentException(
                    "다른 가게 상품은 함께 주문할 수 없습니다. 기존 장바구니를 비우고 다시 시도하세요."
            );
        }

        // 4. 카트에 상품 추가 (중복이면 수량 증가)
        cart.addItem(item);

        // 5. Redis에 저장 (TTL 설정: 1시간)
        // 이유: 사용자가 활동 중이면 TTL을 계속 연장
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRY);
    }

    /**
     * 사용자의 현재 장바구니 조회
     *
     * @param userId 사용자 ID
     * @return 사용자의 장바구니 (없으면 빈 Cart 반환)
     */
    public Cart getCart(Long userId) {
        String key = buildCartKey(userId);

        // Redis에서 조회
        Object cached = redisTemplate.opsForValue().get(key);

        // instanceof 체크로 안전한 캐스팅
        if (cached instanceof Cart) {
            return (Cart) cached;
        }

        // 캐시가 없으면 빈 새 Cart 반환
        // 이유: 클라이언트가 null 처리 하지 않아도 됨
        return createNewCart(userId);
    }

    /**
     * 장바구니에서 특정 상품 제거
     *
     * 비즈니스 로직:
     * 1. Redis에서 카트 조회
     * 2. 해당 상품 제거
     * 3. 장바구니가 비면 storeId 초기화
     * 4. Redis에 저장
     *
     * 주의: 없는 상품 제거 시 에러 없음 (idempotent)
     *
     * @param userId 사용자 ID
     * @param menuItemId 제거할 상품 ID
     */
    public void removeItem(Long userId, Object menuItemId) {
        if (menuItemId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }

        String key = buildCartKey(userId);

        // Redis에서 기존 카트 조회
        Object cached = redisTemplate.opsForValue().get(key);

        // instanceof로 안전하게 타입 확인
        if (cached instanceof Cart) {
            Cart cart = (Cart) cached;

            // 카트에서 해당 상품 제거 (도메인 로직)
            cart.removeItem(menuItemId);

            // 수정된 카트를 Redis에 저장 (TTL 갱신)
            redisTemplate.opsForValue().set(key, cart, CART_EXPIRY);
        }
        // 주의: 카트가 없어도 에러 없음 (이미 제거된 상태로 간주)
    }

    /**
     * 사용자의 장바구니 완전 초기화
     *
     * @param userId 사용자 ID
     */
    public void clearCart(Long userId) {
        // Redis에서 해당 키 삭제 (TTL 대기 없이 즉시 제거)
        redisTemplate.delete(buildCartKey(userId));
    }

    /**
     * 사용자별 Redis 카트 키 생성
     *
     * 규칙: "cart:{userId}"
     * 예: userId=1 → "cart:1"
     *
     * 이유:
     * - 키 생성 규칙을 한 곳에서만 관리
     * - 규칙 변경 시 모든 메서드에 자동 적용
     *
     * @param userId 사용자 ID
     * @return Redis 키
     */
    private String buildCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * 새로운 Cart 객체 생성
     *
     * 이유:
     * - 새 객체 생성 로직을 분리하여 변경 시 한 곳만 수정
     * - UUID 생성은 비용이 크므로 필요할 때만 호출
     *
     * @param userId 사용자 ID
     * @return 새로 생성된 빈 Cart
     */
    private Cart createNewCart(Long userId) {
        return Cart.builder()
                // UUID: userId의 해시값으로 생성 (동일한 userId → 동일한 UUID)
                .userId(UUID.nameUUIDFromBytes(userId.toString().getBytes()))
                .build();
    }
}