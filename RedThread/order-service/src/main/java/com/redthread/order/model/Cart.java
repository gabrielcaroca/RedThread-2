package com.redthread.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "carts")
public class Cart {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable=false, unique = true) private String userId;
  @Column(nullable=false) private Instant createdAt;
  @Column(nullable=false) private Instant updatedAt;
}
