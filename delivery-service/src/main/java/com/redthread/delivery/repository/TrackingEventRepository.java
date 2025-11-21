package com.redthread.delivery.repository;

import com.redthread.delivery.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByShipmentIdOrderByCreatedAtAsc(Long shipmentId);
}
