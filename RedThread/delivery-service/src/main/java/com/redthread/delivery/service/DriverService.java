package com.redthread.delivery.service;

import com.redthread.delivery.domain.*;
import java.util.List;

public interface DriverService {
    Driver create(String name, String phone, String email, Boolean active);
    List<Driver> list();
    Driver update(Long id, String name, String phone, String email, Boolean active);
}










