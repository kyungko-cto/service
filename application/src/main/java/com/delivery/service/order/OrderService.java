package com.delivery.service.order;


import com.delivery.domain.order.ItemLine;
import com.delivery.domain.order.Order;
import com.delivery.domain.order.OrderStatus;
import com.delivery.db.entity.order.OrderEntity;
import com.delivery.db.entity.order.OrderRepository;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public UUID create(UUID userId, UUID storeId, List<ItemLine> items) {
        UUID orderId = UUID.randomUUID();
        OrderEntity entity = new OrderEntity();
        entity.setId(orderId);
        entity.setUserId(userId);
        entity.setStoreId(storeId);
        entity.setStatus(OrderStatus.CREATED.name());
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setTotalAmount(items.stream().mapToInt(ItemLine::getLineAmount).sum());
        orderRepository.save(entity);
        return orderId;
    }

    public Optional<Order> getById(UUID orderId) {
        return orderRepository.findById(orderId).map(this::toDomain);
    }

    public void cancel(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        entity.setStatus(OrderStatus.CANCELLED.name());
        orderRepository.save(entity);
    }

    public void markPaid(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        entity.setStatus(OrderStatus.PAID.name());
        orderRepository.save(entity);
    }

    public void markDelivered(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        entity.setStatus(OrderStatus.DELIVERED.name());
        orderRepository.save(entity);
    }

    private Order toDomain(OrderEntity entity) {
        return Order.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .storeId(entity.getStoreId())
                .status(OrderStatus.valueOf(entity.getStatus()))
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

