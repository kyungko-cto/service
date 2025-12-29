package com.delivery.api.domain.payment;


import com.delivery.api.ApiResponse;
import com.delivery.api.domain.payment.converter.PaymentConverter;
import com.delivery.api.domain.payment.dto.PaymentRequest;
import com.delivery.api.domain.payment.dto.PaymentResponse;
import com.delivery.application.payment.PaymentService;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> request(@RequestBody PaymentRequest request) {
        UUID paymentId = paymentService.request(request.orderId(), request.amount(), request.provider());
        var payment = paymentService.getById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED));
        return ApiResponse.ok(PaymentConverter.toResponse(payment));
    }

    @PostMapping("/{paymentId}/success")
    public ResponseEntity<ApiResponse<PaymentResponse>> success(@PathVariable UUID paymentId,
                                                                @RequestParam String transactionId) {
        paymentService.markSuccess(paymentId, transactionId);
        var payment = paymentService.getById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED));
        return ApiResponse.ok(PaymentConverter.toResponse(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> get(@PathVariable UUID paymentId) {
        var payment = paymentService.getById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED));
        return ApiResponse.ok(PaymentConverter.toResponse(payment));
    }
}

