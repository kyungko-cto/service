package com.delivery.api.domain.cart.controller;

import com.delivery.api.ApiResponse;
import com.delivery.api.domain.cart.converter.CartConverter;
import com.delivery.api.domain.cart.dto.CartItemRequest;
import com.delivery.api.domain.cart.dto.CartResponse;
import com.delivery.api.domain.cart.dto.RemoveItemRequest;
import com.delivery.auth.model.UserPrincipal;
import com.delivery.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니에 상품 추가
     * 응답: 추가 후 전체 장바구니 반환 (클라이언트가 최신 상태 확인 가능)
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CartItemRequest request) {

        // CartItemRequest → CartItem 도메인 객체로 변환
        var cartItem = CartConverter.toDomain(request);

        // 서비스 계층에서 비즈니스 로직 처리
        // 리팩토링: UUID를 Long으로 변환 (CartService는 Long을 사용)
        // TODO: CartService도 UUID를 사용하도록 변경하는 것이 더 나음
        Long userId = convertUuidToLong(user.getId());
        cartService.addItem(userId, cartItem);

        // 추가 후 최신 장바구니 상태 조회하여 반환
        // 이유: 클라이언트가 아이템 추가 후 전체 장바구니 상태를 바로 알 수 있음
        var updatedCart = cartService.getCart(userId);
        var response = CartConverter.toResponse(updatedCart);

        return ApiResponse.success(response);
    }

    /**
     * UUID를 Long으로 변환하는 헬퍼 메서드
     * 
     * 리팩토링: CartService가 Long을 사용하므로 임시 변환
     * TODO: CartService도 UUID를 사용하도록 변경하는 것이 더 나음
     */
    private Long convertUuidToLong(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        // UUID의 최상위 비트를 사용하여 Long으로 변환 (임시 방법)
        long mostSignificantBits = uuid.getMostSignificantBits();
        return mostSignificantBits;
    }

    /**
     * 전체 장바구니 조회
     * 응답: 사용자의 현재 장바구니 정보
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal user) {

        // 서비스에서 Cart 도메인 객체 조회
        Long userId = convertUuidToLong(user.getId());
        var cart = cartService.getCart(userId);

        // Cart → CartResponse DTO로 변환하여 API 응답
        var response = CartConverter.toResponse(cart);

        return ApiResponse.success(response);
    }

    /**
     * 장바구니에서 특정 상품 제거
     * 응답: 제거 후 최신 장바구니 반환
     */
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody RemoveItemRequest request) {

        // 서비스에서 특정 상품 제거
        Long userId = convertUuidToLong(user.getId());
        cartService.removeItem(userId, request.getMenuItemId());

        // 제거 후 최신 장바구니 상태 반환
        var updatedCart = cartService.getCart(userId);
        var response = CartConverter.toResponse(updatedCart);

        return ApiResponse.success(response);
    }

    /**
     * 전체 장바구니 초기화
     * 응답: 성공 메시지 (빈 장바구니)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal UserPrincipal user) {

        // 서비스에서 장바구니 전체 삭제
        Long userId = convertUuidToLong(user.getId());
        cartService.clearCart(userId);

        // 초기화된 빈 장바구니 반환
        var emptyCart = cartService.getCart(userId);
        var response = CartConverter.toResponse(emptyCart);

        return ApiResponse.success(response);
    }
}