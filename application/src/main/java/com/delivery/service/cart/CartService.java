package com.delivery.service.cart;

import com.delivery.domain.cart.Cart;
import com.delivery.domain.cart.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";

    public void addItem(Long userId, CartItem item) {
        String key = CART_KEY_PREFIX + userId;
        Cart cart = (Cart) redisTemplate.opsForValue().get(key);

        if (cart == null) {
            cart = Cart.builder()
                    .userId(UUID.nameUUIDFromBytes(userId.toString().getBytes()))
                    .build();
        }
        cart.addItem(item);

        // 메모리 누수 방지: 1시간 뒤 자동 삭제
        redisTemplate.opsForValue().set(key, cart, Duration.ofHours(1));
    }

    public Cart getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        return (Cart) redisTemplate.opsForValue().getOrDefault(key,
                Cart.builder().userId(UUID.nameUUIDFromBytes(userId.toString().getBytes())).build());
    }

    public void clearCart(Long userId) {
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }
}