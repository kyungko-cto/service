package com.delivery.api.domain.delivery.controller;

import com.delivery.api.ApiResponse;
import com.delivery.api.domain.delivery.converter.DeliveryConverter;
import com.delivery.api.domain.delivery.dto.DeliveryRequest;
import com.delivery.api.domain.delivery.dto.DeliveryResponse;
import com.delivery.service.delivery.DeliveryService;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import com.delivery.domain.delivery.Delivery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 배달원을 배정합니다.
     * 
     * @param request 배달 배정 요청
     * @return 배달 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryResponse>> assign(
            @RequestBody DeliveryRequest request) {
        UUID deliveryId = deliveryService.assign(
                request.getOrderId(),
                request.getRiderName(),
                request.getDestinationAddressId()
        );
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    /**
     * 배달원이 픽업 완료 상태로 변경합니다.
     * 
     * @param deliveryId 배달 ID
     * @return 배달 정보
     */
    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pickUp(@PathVariable UUID deliveryId) {
        deliveryService.pickUp(deliveryId);
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    /**
     * 배달을 완료 상태로 변경합니다.
     * 
     * @param deliveryId 배달 ID
     * @return 배달 정보
     */
    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<ApiResponse<DeliveryResponse>> complete(@PathVariable UUID deliveryId) {
        deliveryService.complete(deliveryId);
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    /**
     * 배달을 조회합니다.
     * 
     * @param deliveryId 배달 ID
     * @return 배달 정보
     */
    @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> get(@PathVariable UUID deliveryId) {
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    private Delivery findDelivery(UUID deliveryId) {
        return deliveryService.getById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
    }
}
