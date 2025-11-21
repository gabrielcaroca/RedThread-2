package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "drivers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Driver {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=120)
    private String name;
    @Column(nullable=false, length=40)
    private String phone;
    @Column(length=140)
    private String email;
    @Column(nullable=false)
    private boolean active = true;
}
