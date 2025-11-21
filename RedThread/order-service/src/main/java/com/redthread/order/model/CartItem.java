package com.redthread.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "cart_items", uniqueConstraints = {
  @UniqueConstraint(name="uq_cart_variant", columnNames = {"cart_id","variant_id"})
})
public class CartItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="cart_id")
  private Cart cart;
  @Column(nullable=false) private Long variantId;
  @Column(nullable=false) private Integer quantity;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal unitPrice;
}