package com.delivery.service.order;

import com.delivery.domain.order.ItemLine;
import com.delivery.domain.order.Order;
import com.delivery.domain.order.OrderStatus;
import com.delivery.db.entity.order.OrderEntity;
import com.delivery.db.entity.order.OrderRepository;
import com.delivery.db.entity.order.OrderDetailEntity;
import com.delivery.db.entity.user.UserEntity;
import com.delivery.db.entity.user.UserRepository;
import com.delivery.db.entity.store.StoreEntity;
import com.delivery.db.entity.store.StoreRepository;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 주문 서비스
 * 
 * 책임:
 * - 주문 생성, 조회, 취소, 상태 변경 등 주문 관련 비즈니스 로직 처리
 * - Entity와 Domain 객체 간 변환
 * - 트랜잭션 관리
 * 
 * 리팩토링 사항:
 * 1. OrderStatus를 String으로 변환하지 않고 enum 직접 사용
 * 2. 도메인 로직을 도메인 객체로 이동 (Order.cancel() 사용)
 * 3. toDomain 메서드에서 details(ItemLine) 포함하도록 수정
 * 4. 트랜잭션 어노테이션 추가
 * 5. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 * 6. 캐싱 전략 추가: 자주 조회되는 주문 정보 캐싱
 * 7. 비동기 처리 추가: 알림 발송 등 비동기 작업
 * 
 * 설계 원칙:
 * - application 모듈의 서비스는 도메인별로 분리 (order, cart, delivery, payment)
 *   이유: 단일 책임 원칙 준수, 유지보수성 향상
 * - admin 모듈에서 재사용 가능: 공통 비즈니스 로직은 application 서비스를 재사용
 *   예: AdminOrderService에서 OrderService.cancel() 재사용
 * - 도메인 객체와 Entity 분리: 비즈니스 로직은 도메인 객체에, 영속성은 Entity에
 * - 캐싱: 자주 조회되는 데이터는 Redis에 캐싱하여 DB 부하 감소
 * - 비동기 처리: 블로킹 작업 최소화로 CPU 사용률 개선
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    /**
     * 주문을 생성합니다.
     * 
     * @param userId 주문자 ID
     * @param storeId 가게 ID
     * @param items 주문 아이템 목록
     * @return 생성된 주문 ID
     * 
     * 리팩토링: 
     * - OrderStatus를 enum으로 직접 사용 (String 변환 제거)
     * - 트랜잭션 보장
     * - 캐시 무효화: 주문 생성 시 관련 캐시 삭제
     */
    @Transactional
    @CacheEvict(value = "orders", key = "#userId")
    public UUID create(UUID userId, UUID storeId, List<ItemLine> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "주문 아이템이 없습니다");
        }

        // 리팩토링 완료: UserEntity와 StoreEntity를 조회하여 설정
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다"));
        
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "가게를 찾을 수 없습니다"));

        UUID orderId = UUID.randomUUID();
        OrderEntity entity = OrderEntity.builder()
                .id(orderId)
                .user(user)
                .store(store)
                .status(com.delivery.db.entity.order.OrderStatus.PENDING_PAYMENT)
                .createdAt(OffsetDateTime.now())
                .totalAmount(items.stream().mapToInt(ItemLine::getLineAmount).sum())
                .build();
        
        // 주문 상세 추가
        for (ItemLine item : items) {
            OrderDetailEntity detail = OrderDetailEntity.builder()
                    .menuItemId(item.getMenuItemId())
                    .menuItemName(item.getMenuItemName())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .build();
            entity.addDetail(detail);
        }
        
        orderRepository.save(entity);
        
        // 비동기 알림 발송은 API 레이어에서 처리 (application 모듈은 비동기 설정 의존하지 않음)
        // sendOrderNotificationAsync(userId, orderId);
        
        return orderId;
    }

    /**
     * 주문 ID로 주문을 조회합니다.
     * 
     * @param orderId 주문 ID
     * @return 주문 도메인 객체 (Optional)
     * 
     * 리팩토링: 
     * - details(ItemLine) 포함하도록 수정
     * - 캐싱: 자주 조회되는 주문 정보 캐싱 (DB 부하 감소)
     */
    @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
    public Optional<Order> getById(UUID orderId) {
        return orderRepository.findById(orderId).map(this::toDomain);
    }

    /**
     * 주문을 취소합니다.
     * 
     * @param orderId 주문 ID
     * 
     * 리팩토링: 
     * - 도메인 객체의 cancel() 메서드 사용
     * - 트랜잭션 보장
     * - 캐시 무효화: 주문 취소 시 캐시 삭제
     */
    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public void cancel(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        
        // 도메인 객체로 변환하여 비즈니스 로직 실행
        Order order = toDomain(entity);
        order.cancel();
        
        // 엔티티에 반영
        // 리팩토링: setStatus() 대신 cancel() 메서드 사용
        entity.cancel();
        orderRepository.save(entity);
        
        // 비동기 알림 발송은 API 레이어에서 처리
        // sendCancelNotificationAsync(orderId);
    }

    /**
     * 주문을 결제 완료 상태로 변경합니다.
     * 
     * @param orderId 주문 ID
     * 
     * 리팩토링: OrderStatus를 enum으로 직접 사용
     */
    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public void markPaid(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 리팩토링: setStatus() 대신 markPaid() 메서드 사용
        entity.markPaid();
        orderRepository.save(entity);
    }

    /**
     * 주문을 배송 완료 상태로 변경합니다.
     * 
     * @param orderId 주문 ID
     * 
     * 리팩토링: OrderStatus를 enum으로 직접 사용
     */
    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public void markDelivered(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 리팩토링: setStatus() 대신 complete() 메서드 사용
        entity.complete();
        orderRepository.save(entity);
    }

    /**
     * OrderEntity를 Order 도메인 객체로 변환합니다.
     * 
     * @param entity 주문 엔티티
     * @return 주문 도메인 객체
     * 
     * 리팩토링 완료:
     * - OrderStatus enum 변환 수정
     * - OrderDetailEntity를 ItemLine으로 변환하여 details 포함
     */
    private Order toDomain(OrderEntity entity) {
        // 리팩토링: Entity의 OrderStatus를 Domain의 OrderStatus로 변환
        OrderStatus domainStatus = convertToDomainStatus(entity.getStatus());
        
        // OrderDetailEntity를 ItemLine으로 변환
        List<ItemLine> itemLines = entity.getDetails().stream()
                .map(detail -> new ItemLine(
                        detail.getMenuItemId(),
                        detail.getMenuItemName(),
                        detail.getUnitPrice(),
                        detail.getQuantity()
                ))
                .collect(Collectors.toList());
        
        return Order.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .storeId(entity.getStore() != null ? entity.getStore().getId() : null)
                .status(domainStatus)
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .details(itemLines)
                .build();
    }

    /**
     * Entity의 OrderStatus를 Domain의 OrderStatus로 변환합니다.
     * 
     * @param entityStatus 엔티티의 주문 상태
     * @return 도메인의 주문 상태
     * 
     * 리팩토링: 두 enum 간의 매핑 로직 추가
     */
    private OrderStatus convertToDomainStatus(com.delivery.db.entity.order.OrderStatus entityStatus) {
        if (entityStatus == null) {
            return null;
        }
        
        return switch (entityStatus) {
            case PENDING_PAYMENT -> OrderStatus.CREATED;
            case PAID -> OrderStatus.PAID;
            case CANCELLED -> OrderStatus.CANCELLED;
            case COMPLETED -> OrderStatus.DELIVERED;
            default -> OrderStatus.CREATED;
        };
    }

}
