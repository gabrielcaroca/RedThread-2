package com.redthread.order.dto;


public record AddressRes(
    Long id, 
    String line1, 
    String line2, 
    String city, 
    String state,
    String zip, 
    String country, 
    boolean isDefault
    ) {}
