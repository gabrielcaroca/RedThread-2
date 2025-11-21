package com.redthread.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="order_items")
public class OrderItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="order_id")
  private Order order;
  @Column(nullable=false) private Long variantId;
  @Column(nullable=false) private Integer quantity;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal unitPrice;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal lineTotal;
}
