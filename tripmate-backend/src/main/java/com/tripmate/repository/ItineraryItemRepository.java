package com.tripmate.repository;

import com.tripmate.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, UUID> {
    List<ItineraryItem> findByDayIdOrderByPosition(UUID dayId);
    long countByDayId(UUID dayId);
}
