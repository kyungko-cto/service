package com.delivery.common.constant;

import lombok.Getter;
import lombok.Setter;

/**
 * 페이지 요청 공통 클래스
 * 
 * 리팩토링: 사용하지 않는 필드 제거 또는 사용하도록 개선
 * 현재는 Spring Data의 Pageable을 사용하므로 이 클래스는 사용되지 않음
 * 향후 커스텀 페이지 요청이 필요하면 사용 가능
 */
@Getter
@Setter
public class PageRequest {

    private int page = 0;      // 기본값: 0 (첫 페이지)
    private int pageSize = 20; // 기본값: 20 (페이지당 항목 수)
    
    /**
     * Spring Data의 Pageable로 변환
     * 
     * @return Pageable 객체
     */
    public org.springframework.data.domain.Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(page, pageSize);
    }
}
