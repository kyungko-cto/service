package com.delivery.api.domain.cart.controller;



import com.delivery.api.ApiResponse;
import com.delivery.api.domain.cart.converter.CartConverter;
import com.delivery.api.domain.cart.dto.CartItemRequest;
import com.delivery.api.domain.cart.dto.CartResponse;
import com.delivery.api.domain.cart.dto.RemoveItemRequest;
import com.delivery.auth.dto.UserRequest;
import com.delivery.service.cart.CartService;
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
        cartService.addItem(user.getId(), CartConverter.toDomain(request));//토큰에서 받은 유저도 컨버터로 해야하지않나
        return ApiResponse.success(null);//아이템 추가한거에 대한 카트를 반환해야하지않나
        //return cartResponse를 만들어서
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
