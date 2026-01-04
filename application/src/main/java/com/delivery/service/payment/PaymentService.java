package com.delivery.service.payment;

import com.delivery.domain.payment.Payment;
import com.delivery.domain.payment.PaymentStatus;
import com.delivery.db.entity.payment.PaymentEntity;
import com.delivery.db.entity.payment.PaymentRepository;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 결제 서비스
 * 
 * 책임:
 * - 결제 요청, 조회, 성공/실패 처리 등 결제 관련 비즈니스 로직 처리
 * - Entity와 Domain 객체 간 변환
 * - 트랜잭션 관리
 * 
 * 리팩토링 사항:
 * 1. PaymentStatus를 enum으로 직접 사용
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
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제를 요청합니다.
     * 
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param provider 결제 수단 (예: "CARD", "ACCOUNT_TRANSFER")
     * @return 생성된 결제 ID
     * 
     * 리팩토링:
     * - PaymentStatus를 enum으로 직접 사용
     * - 트랜잭션 보장
     */
    @Transactional
    public UUID request(UUID orderId, int amount, String provider) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "결제 금액은 0보다 커야 합니다");
        }
        if (provider == null || provider.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "결제 수단은 필수입니다");
        }

        UUID paymentId = UUID.randomUUID();
        PaymentEntity entity = PaymentEntity.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(amount)
                .status(com.delivery.db.entity.payment.PaymentStatus.PENDING)
                .requestedAt(OffsetDateTime.now())
                .provider(provider)
                .build();
        
        paymentRepository.save(entity);
        return paymentId;
    }

    /**
     * 결제 ID로 결제를 조회합니다.
     * 
     * @param paymentId 결제 ID
     * @return 결제 도메인 객체 (Optional)
     */
    public Optional<Payment> getById(UUID paymentId) {
        return paymentRepository.findById(paymentId).map(this::toDomain);
    }

    /**
     * 결제를 성공 상태로 변경합니다.
     * 
     * @param paymentId 결제 ID
     * @param transactionId 거래 ID
     * 
     * 리팩토링:
     * - 도메인 객체의 markSuccess() 메서드 사용
     * - Entity의 markSuccess() 메서드 활용
     */
    @Transactional
    public void markSuccess(UUID paymentId, String transactionId) {
        PaymentEntity entity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED, "결제를 찾을 수 없습니다"));
        
        // 도메인 객체로 변환하여 비즈니스 로직 실행
        Payment payment = toDomain(entity);
        payment.markSuccess(transactionId);
        
        // 엔티티에 반영
        entity.markSuccess(transactionId);
        paymentRepository.save(entity);
    }

    /**
     * 결제를 실패 상태로 변경합니다.
     * 
     * @param paymentId 결제 ID
     * 
     * 리팩토링:
     * - 도메인 객체의 markFailed() 메서드 사용
     * - Entity의 markFailed() 메서드 활용
     */
    @Transactional
    public void markFailed(UUID paymentId) {
        PaymentEntity entity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED, "결제를 찾을 수 없습니다"));
        
        // 도메인 객체로 변환하여 비즈니스 로직 실행
        Payment payment = toDomain(entity);
        payment.markFailed();
        
        // 엔티티에 반영
        entity.markFailed();
        paymentRepository.save(entity);
    }

    /**
     * PaymentEntity를 Payment 도메인 객체로 변환합니다.
     * 
     * @param entity 결제 엔티티
     * @return 결제 도메인 객체
     * 
     * 리팩토링:
     * - PaymentStatus enum 변환 수정
     */
    private Payment toDomain(PaymentEntity entity) {
        // 리팩토링: Entity의 PaymentStatus를 Domain의 PaymentStatus로 변환
        PaymentStatus domainStatus = convertToDomainStatus(entity.getStatus());
        
        return Payment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .amount(entity.getAmount())
                .status(domainStatus)
                .provider(entity.getProvider())
                .transactionId(entity.getTransactionId())
                .build();
    }

    /**
     * Entity의 PaymentStatus를 Domain의 PaymentStatus로 변환합니다.
     * 
     * @param entityStatus 엔티티의 결제 상태
     * @return 도메인의 결제 상태
     */
    private PaymentStatus convertToDomainStatus(com.delivery.db.entity.payment.PaymentStatus entityStatus) {
        if (entityStatus == null) {
            return null;
        }
        
        return switch (entityStatus) {
            case PENDING -> PaymentStatus.PENDING;
            case SUCCESS -> PaymentStatus.SUCCESS;
            case FAILED -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }
}

