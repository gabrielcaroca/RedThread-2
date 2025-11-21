package com.redthread.order.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "addresses")
public class Address {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable=false) private String userId;
  @Column(nullable=false) private String line1;
  private String line2;
  @Column(nullable=false) private String city;
  @Column(nullable=false) private String state;
  @Column(nullable=false) private String zip;
  @Column(nullable=false) private String country;
  @Column(nullable=false) private boolean isDefault;
}









