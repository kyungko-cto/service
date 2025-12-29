package com.delivery.api.domain.delivery.controller;

import com.delivery.api.ApiResponse;
import com.delivery.api.domain.delivery.converter.DeliveryConverter;
import com.delivery.api.domain.delivery.dto.DeliveryRequest;
import com.delivery.api.domain.delivery.dto.DeliveryResponse;
import com.delivery.application.delivery.DeliveryService;
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

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryResponse>> assign(@RequestBody DeliveryRequest request) {
        Delivery deliveryDomain = DeliveryConverter.toDomain(request);
        UUID deliveryId = deliveryService.assign(
                deliveryDomain.getOrderId(),
                deliveryDomain.getRiderName(),
                deliveryDomain.getDestinationAddressId()
        );
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pickUp(@PathVariable UUID deliveryId) {
        deliveryService.pickUp(deliveryId);
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<ApiResponse<DeliveryResponse>> complete(@PathVariable UUID deliveryId) {
        deliveryService.complete(deliveryId);
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> get(@PathVariable UUID deliveryId) {
        return ApiResponse.success(DeliveryConverter.toResponse(findDelivery(deliveryId)));
    }

    private Delivery findDelivery(UUID deliveryId) {
        return deliveryService.getById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
    }
}
