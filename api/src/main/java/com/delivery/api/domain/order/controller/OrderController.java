package com.delivery.api.domain.order.controller;



import com.delivery.api.ApiResponse;
import com.delivery.api.domain.order.converter.OrderConverter;
import com.delivery.api.domain.order.dto.CreateOrderRequest;
import com.delivery.api.domain.order.dto.OrderResponse;
import com.delivery.api.domain.order.dto.CancelOrderRequest;
import com.delivery.application.order.OrderService;
import com.delivery.auth.dto.UserRequest;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@AuthenticationPrincipal UserRequest user,
                                                             @RequestBody CreateOrderRequest request) {
        UUID orderId = orderService.create(user.getId(), request.getStoreId(),
                request.getItems().stream().map(OrderConverter::toDomainItem).toList());
        var order = orderService.getById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return ApiResponse.ok(OrderConverter.toResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable UUID orderId) {
        var order = orderService.getById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return ApiResponse.ok(OrderConverter.toResponse(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable UUID orderId,
                                                    @RequestBody CancelOrderRequest request) {
        orderService.cancel(orderId);
        return ApiResponse.ok(null);
    }
}
