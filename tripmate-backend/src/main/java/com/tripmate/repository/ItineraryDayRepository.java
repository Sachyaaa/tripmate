package com.tripmate.repository;

import com.tripmate.entity.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, UUID> {
    List<ItineraryDay> findByTripIdOrderByDayNumber(UUID tripId);
}
