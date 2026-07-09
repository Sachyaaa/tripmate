package com.tripmate.repository;

import com.tripmate.entity.ActivityLog;
import com.tripmate.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    boolean existsByEventId(UUID eventId);
    List<ActivityLog> findByTripIdOrderByCreatedAtDesc(UUID tripId);
}
