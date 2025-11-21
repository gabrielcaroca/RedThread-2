package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "geo_zones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeoZone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=120)
    private String name;
    private String city;
    private String state;
    private String country;
    @Column(name="zip_pattern")
    private String zipPattern; // regex simple
}
