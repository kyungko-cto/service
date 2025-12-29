package com.delivery.api.domain.cart.controller;



import com.delivery.api.ApiResponse;
import com.delivery.api.domain.cart.converter.CartConverter;
import com.delivery.api.domain.cart.dto.CartItemRequest;
import com.delivery.api.domain.cart.dto.CartResponse;
import com.delivery.api.domain.cart.dto.RemoveItemRequest;
import com.delivery.application.cart.CartService;
import com.delivery.auth.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addItem(@AuthenticationPrincipal UserRequest user,
                                                     @RequestBody CartItemRequest request) {
        cartService.addItem(user.getId(), CartConverter.toDomain(request));
        return ApiResponse.success(null);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserRequest user) {
        var cart = cartService.getCart(user.getId());
        return ApiResponse.success(CartConverter.toResponse(cart));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<Void>> removeItem(@AuthenticationPrincipal UserRequest user,
                                                        @RequestBody RemoveItemRequest request) {
        cartService.removeItem(user.getId(), request.menuItemId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserRequest user) {
        cartService.clearCart(user.getId());
        return ApiResponse.success(null);
    }
}
