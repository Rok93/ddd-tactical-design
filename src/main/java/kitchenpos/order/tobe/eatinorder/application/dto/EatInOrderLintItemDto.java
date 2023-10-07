package kitchenpos.order.tobe.eatinorder.application.dto;

import java.math.BigDecimal;
import java.util.UUID;
import kitchenpos.order.tobe.eatinorder.domain.EatInOrder;
import kitchenpos.order.tobe.eatinorder.domain.EatInOrderLineItem;

public class EatInOrderLintItemDto {
    private final UUID menuId;
    private final BigDecimal price;
    private long quantity;

    public EatInOrderLintItemDto(UUID menuId, BigDecimal price, long quantity) {
        this.menuId = menuId;
        this.price = price;
        this.quantity = quantity;
    }

    public static EatInOrderLintItemDto of(EatInOrderLineItem entity) {
        return new EatInOrderLintItemDto(entity.getMenuId(), entity.getPrice(), entity.getQuantity());
    }

    public UUID getMenuId() {
        return menuId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }
}