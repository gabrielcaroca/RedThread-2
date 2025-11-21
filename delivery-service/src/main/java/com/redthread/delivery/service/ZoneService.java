package com.redthread.delivery.service;

import com.redthread.delivery.domain.*;
import java.util.List;

public interface ZoneService {
    GeoZone create(String name, String city, String state, String country, String zipPattern);
    List<GeoZone> list();
}
