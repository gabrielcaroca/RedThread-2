package com.redthread.order.service;

import com.redthread.order.dto.AddItemReq;
import com.redthread.order.dto.CartRes;
import com.redthread.order.dto.UpdateQtyReq;
import com.redthread.order.integrations.CatalogClient;
import com.redthread.order.model.Cart;
import com.redthread.order.model.CartItem;
import com.redthread.order.repository.CartItemRepository;
import com.redthread.order.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepo;
    @Mock CartItemRepository itemRepo;
    @Mock CatalogClient catalog;

    @InjectMocks CartService service;

    @Test
    void getCart_createsCartWhenMissing() {
        String userId = "u1";

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepo.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(itemRepo.findByCartId(1L)).thenReturn(List.of());

        CartRes res = service.getCart(userId);

        assertThat(res.cartId()).isEqualTo(1L);
        assertThat(res.items()).isEmpty();
        assertThat(res.total()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void updateItem_itemNotFound_throws() {
        String userId = "u1";

        Cart cart = Cart.builder()
                .id(1L)
                .userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepo.findByIdAndCartId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateItem(userId, 5L, new UpdateQtyReq(2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addItem_happyPath() {
        String userId = "u1";
        Cart cart = Cart.builder()
                .id(1L).userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        AddItemReq req = new AddItemReq(10L, 2);

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Método real en tu ZIP → findVariantById
        when(catalog.findVariantById(10L))
                .thenReturn(new CatalogClient.VariantInfo(
                        10L,
                        new BigDecimal("1000.00"),
                        50
                ));

        when(itemRepo.findByCartIdAndVariantId(1L, 10L))
                .thenReturn(Optional.empty());

        when(itemRepo.save(any(CartItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(itemRepo.findByCartId(1L))
                .thenReturn(List.of(
                        CartItem.builder()
                                .id(7L)
                                .cart(cart)
                                .variantId(10L)
                                .quantity(2)
                                .unitPrice(new BigDecimal("1000.00"))
                                .build()
                ));

        CartRes res = service.addItem(userId, req);

        assertThat(res.items()).hasSize(1);
        assertThat(res.total()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }
}
