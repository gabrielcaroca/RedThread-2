package com.redthread.order.service;

import com.redthread.order.dto.CheckoutReq;
import com.redthread.order.integrations.CatalogClient;
import com.redthread.order.model.*;
import com.redthread.order.repository.*;
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
class CheckoutServiceTest {

    @Mock CartRepository cartRepo;
    @Mock CartItemRepository itemRepo;
    @Mock AddressRepository addressRepo;
    @Mock OrderRepository orderRepo;
    @Mock OrderItemRepository orderItemRepo;
    @Mock PaymentAttemptRepository payRepo;
    @Mock CatalogClient catalog;

    @InjectMocks CheckoutService service;

    @Test
    void checkout_happyPath() {
        String userId = "u1";

        Cart cart = Cart.builder()
                .id(1L)
                .userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CartItem item = CartItem.builder()
                .id(5L)
                .cart(cart)
                .variantId(10L)
                .quantity(2)
                .unitPrice(new BigDecimal("1000.00"))
                .build();

        Address addr = Address.builder()
                .id(3L)
                .userId(userId)
                .line1("A")
                .city("C")
                .state("S")
                .zip("Z")
                .country("CL")
                .isDefault(true)
                .build();

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepo.findByCartId(1L)).thenReturn(List.of(item));
        when(addressRepo.findByIdAndUserId(3L, userId)).thenReturn(Optional.of(addr));

        // Método real → findVariantById
        when(catalog.findVariantById(10L))
                .thenReturn(new CatalogClient.VariantInfo(
                        10L,
                        new BigDecimal("1000.00"),
                        10
                ));

        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId(99L);
            return o;
        });

        Order out = service.checkout(userId, new CheckoutReq(3L));

        assertThat(out.getId()).isEqualTo(99L);
        assertThat(out.getTotalAmount()).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    void checkout_emptyCart_throws() {
        String userId = "u1";

        Cart cart = Cart.builder()
                .id(1L)
                .userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(cartRepo.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepo.findByCartId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.checkout(userId, new CheckoutReq(1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Carrito vacío");
    }
}
