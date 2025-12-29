package com.delivery.domain.order;

import com.delivery.domain.order.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Order {
    private UUID id;
    private UUID userId;
    private UUID storeId;
    private OrderStatus status;
    private int totalAmount;
    private OffsetDateTime createdAt;
    @Builder.Default
    private List<ItemLine> details = new ArrayList<>();

    public void addItem(ItemLine item) {
        this.details.add(item);
        recalcTotal();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    private void recalcTotal() {
        this.totalAmount = details.stream()
                .mapToInt(ItemLine::getLineAmount)
                .sum();
    }
}
