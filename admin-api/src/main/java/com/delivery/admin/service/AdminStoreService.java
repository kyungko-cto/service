package com.delivery.admin.service;

import com.delivery.admin.dto.StoreListResponse;
import com.delivery.admin.dto.StoreDetailResponse;
import com.delivery.db.entity.store.StoreEntity;
import com.delivery.db.entity.store.StoreRepository;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 관리자 가게 관리 서비스
 * 
 * 리팩토링 사항:
 * 1. 예외 처리 개선: IllegalStateException을 BusinessException으로 변환
 * 2. 필터링 로직 개선: Specification을 사용한 동적 쿼리 (향후 구현)
 * 3. 로깅 추가: 관리자 작업 추적
 * 4. DTO 변환 로직 개선: null 안전성 강화
 * 5. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 * 
 * 설계 원칙:
 * - application 모듈의 서비스를 재사용하지 않고 독립적으로 구현
 *   이유: 가게 관리 기능이 일반 사용자 기능과 다르므로 독립 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private final StoreRepository storeRepository;

    /**
     * 가게 목록 조회
     * 
     * @param pageable 페이징 정보
     * @param status 가게 상태 필터 (선택)
     * @return 가게 목록 (페이징)
     * 
     * 리팩토링 사항:
     * - 문제: 현재는 status 필터링이 구현되지 않음
     * - 해결: 향후 Specification을 사용하여 동적 쿼리 구현 예정
     */
    public Page<StoreListResponse> getStores(Pageable pageable, String status) {
        // TODO: Specification을 사용하여 status 필터링 구현
        // 예시:
        // Specification<StoreEntity> spec = Specification.where(null);
        // if (status != null) {
        //     spec = spec.and((root, query, cb) -> 
        //         cb.equal(root.get("status"), StoreStatus.valueOf(status)));
        // }
        // return storeRepository.findAll(spec, pageable).map(this::toListResponse);
        
        // 현재는 간단한 예시로 전체 조회
        Page<StoreEntity> stores = storeRepository.findAll(pageable);
        return stores.map(this::toListResponse);
    }

    /**
     * 가게 상세 조회
     * 
     * @param storeId 가게 ID
     * @return 가게 상세 정보
     * @throws BusinessException 가게를 찾을 수 없을 때
     */
    public StoreDetailResponse getStore(UUID storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return toDetailResponse(store);
    }

    /**
     * 가게 정지
     * 
     * @param storeId 가게 ID
     * @throws BusinessException 가게를 찾을 수 없거나 정지할 수 없는 상태일 때
     * 
     * 리팩토링 사항:
     * - 문제: StoreEntity.suspend()가 IllegalStateException을 던지는데, 이를 BusinessException으로 변환 필요
     * - 해결: try-catch로 예외를 잡아 BusinessException으로 변환
     * - 로깅: 관리자 작업 추적을 위한 로그 추가
     */
    @Transactional
    public void suspendStore(UUID storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        
        try {
            store.suspend();
            storeRepository.save(store);
            log.info("관리자 작업: 가게 정지 - storeId: {}", storeId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "가게를 정지할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 가게 정지 해제 (영업 상태로 변경)
     * 
     * @param storeId 가게 ID
     * @throws BusinessException 가게를 찾을 수 없거나 활성화할 수 없는 상태일 때
     * 
     * 리팩토링 사항:
     * - 문제: StoreEntity.open()이 IllegalStateException을 던질 수 있음
     * - 해결: try-catch로 예외를 잡아 BusinessException으로 변환
     * - 로깅: 관리자 작업 추적을 위한 로그 추가
     */
    @Transactional
    public void activateStore(UUID storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        
        try {
            store.open(); // 정지 해제 시 영업 상태로 변경
            storeRepository.save(store);
            log.info("관리자 작업: 가게 정지 해제 - storeId: {}", storeId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "가게를 활성화할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * StoreEntity를 StoreListResponse로 변환합니다.
     * 
     * @param store 가게 엔티티
     * @return 가게 목록 응답 DTO
     * 
     * 리팩토링: null 안전성 강화 (enum이 null일 수 있는 경우 대비)
     */
    private StoreListResponse toListResponse(StoreEntity store) {
        return StoreListResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .status(store.getStatus() != null ? store.getStatus().name() : null)
                .phone(store.getPhone())
                .build();
    }

    /**
     * StoreEntity를 StoreDetailResponse로 변환합니다.
     * 
     * @param store 가게 엔티티
     * @return 가게 상세 응답 DTO
     * 
     * 리팩토링:
     * - AddressEntity에서 주소 문자열 추출 (getFullAddress() 사용)
     * - null 안전성 강화 (관계 엔티티와 enum이 null일 수 있는 경우 대비)
     */
    private StoreDetailResponse toDetailResponse(StoreEntity store) {
        // 리팩토링: AddressEntity에서 주소 문자열 추출
        String address = store.getAddress() != null 
                ? store.getAddress().getFullAddress() 
                : null;
        
        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(address)
                .status(store.getStatus() != null ? store.getStatus().name() : null)
                .phone(store.getPhone())
                .ownerId(store.getOwner() != null ? store.getOwner().getId() : null)
                .build();
    }
}

