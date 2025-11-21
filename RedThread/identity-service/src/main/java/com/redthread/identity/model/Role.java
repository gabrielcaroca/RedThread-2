package com.redthread.identity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key", nullable = false, unique = true, length = 40)
    private String key; // CLIENTE, VENDEDOR, REPARTIDOR, ADMIN

    @Column(nullable = false, length = 80)
    private String name;

    // getters/setters
    public Long getId() { return id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
