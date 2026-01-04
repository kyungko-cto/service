package com.delivery.service.delivery;

import com.delivery.domain.delivery.Delivery;
import com.delivery.domain.delivery.DeliveryStatus;
import com.delivery.db.entity.delivery.DeliveryEntity;
import com.delivery.db.entity.delivery.DeliveryRepository;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 배달 서비스
 * 
 * 책임:
 * - 배달원 배정, 픽업, 완료 등 배달 관련 비즈니스 로직 처리
 * - Entity와 Domain 객체 간 변환
 * - 트랜잭션 관리
 * 
 * 리팩토링 사항:
 * 1. DeliveryStatus를 String이 아닌 enum으로 직접 사용
 * 2. 도메인 로직을 도메인 객체로 이동
 * 3. 트랜잭션 어노테이션 추가
 * 4. Entity의 도메인 메서드 활용
 * 5. 주석 추가
 * 
 * 설계 원칙:
 * - application 모듈의 서비스는 도메인별로 분리
 *   이유: 단일 책임 원칙 준수, 유지보수성 향상
 * - 도메인 객체와 Entity 분리: 비즈니스 로직은 도메인 객체에, 영속성은 Entity에
 */
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    /**
     * 배달원을 배정합니다.
     * 
     * @param orderId 주문 ID
     * @param riderName 배달원 이름
     * @param destinationAddressId 배송지 주소 ID
     * @return 생성된 배달 ID
     * 
     * 리팩토링:
     * - DeliveryStatus를 enum으로 직접 사용
     * - Entity의 assign 메서드 활용
     */
    @Transactional
    public UUID assign(UUID orderId, String riderName, UUID destinationAddressId) {
        if (riderName == null || riderName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "배달원 이름은 필수입니다");
        }
        if (destinationAddressId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "배송지 주소 ID는 필수입니다");
        }

        UUID deliveryId = UUID.randomUUID();
        DeliveryEntity entity = DeliveryEntity.builder()
                .id(deliveryId)
                .orderId(orderId)
                .destinationAddressId(destinationAddressId)
                .build();
        
        // 리팩토링: Entity의 도메인 메서드 활용
        entity.assign(riderName);
        
        deliveryRepository.save(entity);
        return deliveryId;
    }

    /**
     * 배달 ID로 배달을 조회합니다.
     * 
     * @param deliveryId 배달 ID
     * @return 배달 도메인 객체 (Optional)
     */
    public Optional<Delivery> getById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId).map(this::toDomain);
    }

    /**
     * 배달원이 픽업 완료 상태로 변경합니다.
     * 
     * @param deliveryId 배달 ID
     * 
     * 리팩토링:
     * - 도메인 객체의 pickUp() 메서드 사용
     * - Entity의 pickUp() 메서드 활용
     */
    @Transactional
    public void pickUp(UUID deliveryId) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        
        // 도메인 객체로 변환하여 비즈니스 로직 실행
        Delivery delivery = toDomain(entity);
        delivery.pickUp();
        
        // 엔티티에 반영
        entity.pickUp();
        deliveryRepository.save(entity);
    }

    /**
     * 배달을 완료 상태로 변경합니다.
     * 
     * @param deliveryId 배달 ID
     * 
     * 리팩토링:
     * - 도메인 객체의 complete() 메서드 사용
     * - Entity의 complete() 메서드 활용
     */
    @Transactional
    public void complete(UUID deliveryId) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        
        // 도메인 객체로 변환하여 비즈니스 로직 실행
        Delivery delivery = toDomain(entity);
        delivery.complete();
        
        // 엔티티에 반영
        entity.complete();
        deliveryRepository.save(entity);
    }

    /**
     * DeliveryEntity를 Delivery 도메인 객체로 변환합니다.
     * 
     * @param entity 배달 엔티티
     * @return 배달 도메인 객체
     * 
     * 리팩토링:
     * - DeliveryStatus enum 변환 수정
     */
    private Delivery toDomain(DeliveryEntity entity) {
        // 리팩토링: Entity의 DeliveryStatus를 Domain의 DeliveryStatus로 변환
        DeliveryStatus domainStatus = convertToDomainStatus(entity.getStatus());
        
        return Delivery.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .riderName(entity.getRiderName())
                .destinationAddressId(entity.getDestinationAddressId())
                .status(domainStatus)
                .assignedAt(entity.getAssignedAt())
                .pickedUpAt(entity.getPickedUpAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    /**
     * Entity의 DeliveryStatus를 Domain의 DeliveryStatus로 변환합니다.
     * 
     * @param entityStatus 엔티티의 배달 상태
     * @return 도메인의 배달 상태
     */
    private DeliveryStatus convertToDomainStatus(com.delivery.db.entity.delivery.DeliveryStatus entityStatus) {
        if (entityStatus == null) {
            return null;
        }
        
        return switch (entityStatus) {
            case ASSIGNED -> DeliveryStatus.ASSIGNED;
            case PICKED_UP -> DeliveryStatus.PICKED_UP;
            case COMPLETED -> DeliveryStatus.COMPLETED;
            default -> DeliveryStatus.ASSIGNED;
        };
    }
}
